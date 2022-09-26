package com.alexandre.proxy;

import com.alexandre.proxy.players.GamePlayer;
import com.alexandre.proxy.utils.ConfigReader;
import com.alexandre.proxy.utils.Configs;
import com.alexandre.proxy.utils.MultiProcessingUtils;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerPool {

    private final File workingDir;
    private final GameType type;
    private final int maxServer;
    private final int firstPort;
    private final int maxPlayers;

    private final CopyOnWriteArrayList<ServerInstance> serverInstances = new CopyOnWriteArrayList<>();

    public ServerPool(File workingDir, GameType type) {
        this.workingDir = workingDir;
        this.type = type;

        Configs configs = ConfigReader.read(new File(workingDir, "pool.config"));
        this.maxPlayers = configs.getSubConfigs("max_players").getIntValue();
        this.maxServer = configs.getSubConfigs("max_server").getIntValue();
        this.firstPort = configs.getSubConfigs("first_port").getIntValue();

        Main.getInstance().getLogger().info("Added server pool \"" + type.toString() + "\" [max players: " + this.maxPlayers + "] [max servers: " + this.maxServer + "] [initial port: " + this.firstPort + "]");
    }

    public void addServer(ProxiedPlayer player) {
        if (this.serverInstances.size() >= this.getMaxServer()) return;

        int port = this.firstPort;
        ArrayList<Integer> usedPorts = new ArrayList<>();
        for (ServerInstance instance : this.serverInstances) {
            usedPorts.add(instance.getInfos().getAddress().getPort());
        }
        while (usedPorts.contains(port)) {
            port++;
        }

        ServerInfo info = Main.getInstance().getProxy().constructServerInfo(
                this.getType() + " " + port,
                new InetSocketAddress("localhost", port),
                "",
                false);

        try {
            File worldsDir = new File(this.workingDir, "worlds");
            File randomWorld = null;
            if (worldsDir.isDirectory()) {
                ArrayList<File> worlds = new ArrayList<>();
                for (File dir : worldsDir.listFiles()) {
                    if (!dir.isDirectory()) continue;
                    worlds.add(dir);
                }
                if (worlds.size() > 0) {
                    randomWorld = worlds.get(new Random().nextInt(worlds.size()));
                }
            }

            ArrayList<String> args = new ArrayList<>();
            args.add("--port");
            args.add("" + port);
            args.add("--max-players");
            args.add("" + this.getMaxPlayers());
            if (randomWorld != null) {
                args.add("--world-dir");
                args.add(worldsDir.getAbsolutePath());
                args.add("--level-name");
                args.add(randomWorld.getName());
            }

            Main.getInstance().getLogger().info("Starting " + info.getName());
            ServerInstance serverInstance = new ServerInstance(info, MultiProcessingUtils.startProcessWithIO(
                    info.getName(),
                    this.workingDir,
                    args.toArray(new String[0])));
            this.serverInstances.add(serverInstance);

            GamePlayer gamePlayer = Main.getPlayer(player);
            if (gamePlayer != null) {
                gamePlayer.setCurrentServer(serverInstance);
                serverInstance.getWaitingPlayers().addIfAbsent(gamePlayer);
            }

            this.checkActive(serverInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkActive(ServerInstance instance) {
        if (instance.isActive()) return;
        instance.getInfos().ping((ping, error) -> {
            if (error != null) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkActive(instance);
                return;
            }
            instance.setActive(true);
        });
    }

    public void checkServers(ProxiedPlayer player, boolean addIfFull) {
        boolean skippedFirst = false;
        boolean allFull = addIfFull;
        for (ServerInstance instance : this.serverInstances) {
            if (!instance.canJoin(Math.max(this.maxPlayers - 2, 1))) continue;
            allFull = false;

            if (instance.getInfos().getPlayers().size() + instance.getWaitingPlayers().size() > 0 || instance.isRemoving()) continue;

            if (!skippedFirst) {
                skippedFirst = true;
                continue;
            }

            this.serverInstances.remove(instance);
            instance.remove();
        }

        if (allFull) {
            this.addServer(player);
        }
    }

    public ServerInstance getRandomServer(ProxiedPlayer player, boolean needAServer) {
        this.checkServers(player, true);

        ServerInstance instance;
        int index = 0;
        do {
            instance = this.serverInstances.get(index);
            index++;
        } while (!instance.canJoin(this.maxPlayers) && index < this.serverInstances.size());

        GamePlayer gamePlayer = Main.getPlayer(player);
        if (gamePlayer != null) gamePlayer.setCurrentServer(instance);
        if (!instance.isActive() && player != null) {
            if (gamePlayer != null) instance.getWaitingPlayers().addIfAbsent(gamePlayer);
            if (needAServer) return GameType.AFK.getPool().getRandomServer(null, false);
            return instance;
        }
        return instance;
    }

    public void closeAll() {
        for (ServerInstance instance : this.getServerInstances()) {
            this.serverInstances.remove(instance);
            instance.remove();
        }
    }

    public File getWorkingDir() {
        return this.workingDir;
    }

    public GameType getType() {
        return this.type;
    }

    public int getMaxServer() {
        return this.maxServer;
    }

    public int getFirstPort() {
        return this.firstPort;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public CopyOnWriteArrayList<ServerInstance> getServerInstances() {
        return this.serverInstances;
    }

}
