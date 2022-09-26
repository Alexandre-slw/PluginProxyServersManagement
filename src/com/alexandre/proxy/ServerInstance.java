package com.alexandre.proxy;

import com.alexandre.proxy.players.GamePlayer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerInstance {

    private final ServerInfo infos;
    private final Process process;
    private boolean removing = false;

    private boolean canJoin = true;
    private boolean active = false;
    private final CopyOnWriteArrayList<GamePlayer> waitingPlayers = new CopyOnWriteArrayList<>();

    public ServerInstance(ServerInfo infos, Process process) {
        this.infos = infos;
        this.process = process;
    }

    public ServerInfo getInfos() {
        return this.infos;
    }

    public Process getProcess() {
        return this.process;
    }

    public boolean isRemoving() {
        return this.removing;
    }

    public void remove() {
        if (this.isRemoving()) return;
        this.removing = true;

        Main.getInstance().getLogger().info("Closing " + this.getInfos().getName());
        for (ProxiedPlayer player : this.getInfos().getPlayers()) {
            if (!player.isConnected()) continue;
            player.connect(GameType.LOBBY.getPool().getRandomServer(player, true).getInfos());
            player.sendMessage(new TextComponent("§cYou server has been closed, you have been sent to the lobby."));
        }

        try {
            ArrayList<String> cmd = new ArrayList<>();
            cmd.add("screen");
            cmd.add("-S");
            cmd.add(this.getInfos().getName().replace(" ", "_"));
            cmd.add("-p");
            cmd.add("0");
            cmd.add("-X");
            cmd.add("stuff");
            cmd.add("--^Mstop^M");

            new ProcessBuilder(cmd).start();
        } catch (Exception e) {
            e.printStackTrace();
            this.getProcess().destroyForcibly();
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (this.active) {
            for (GamePlayer player : this.getWaitingPlayers()) {
                if (!player.getPlayer().isConnected()) continue;
                player.getPlayer().connect(this.getInfos());
            }
            this.getWaitingPlayers().clear();
        }
    }

    public CopyOnWriteArrayList<GamePlayer> getWaitingPlayers() {
        return this.waitingPlayers;
    }

    public boolean canJoin() {
        return this.canJoin;
    }

    public boolean canJoin(int maxPlayers) {
        return this.canJoin && this.getInfos().getPlayers().size() + this.getWaitingPlayers().size() < maxPlayers;
    }

    public void setCanJoin(boolean canJoin) {
        this.canJoin = canJoin;
    }
}
