package com.alexandre.proxy.events;

import com.alexandre.proxy.players.GamePlayer;
import com.alexandre.proxy.GameType;
import com.alexandre.proxy.Main;
import com.alexandre.proxy.ServerInstance;
import com.alexandre.proxy.ServerPool;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinEvent implements Listener {

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if (event.getReason() != ServerConnectEvent.Reason.JOIN_PROXY) return;
        Main.getPlayers().add(new GamePlayer(event.getPlayer()));

        ServerInfo info = GameType.LOBBY.getPool().getRandomServer(event.getPlayer(), true).getInfos();
        event.setTarget(info);
    }

    @EventHandler
    public void onServerConnect(ServerSwitchEvent event) {
        this.closeUnusedServer(event.getFrom(), event.getPlayer());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        GamePlayer player = Main.getPlayer(event.getPlayer());
        if (player != null) {
            if (player.getCurrentServer() != null) {
                player.getCurrentServer().getWaitingPlayers().remove(player);
            }
            Main.getPlayers().remove(player);
        }

        if (event.getPlayer().getServer() == null) return;
        this.closeUnusedServer(event.getPlayer().getServer().getInfo(), event.getPlayer());
    }

    public void closeUnusedServer(ServerInfo server, ProxiedPlayer player) {
        if (server == null || server.getPlayers().size() > 1) return;

        GamePlayer gamePlayer = Main.getPlayer(player);

        BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            for (ServerPool serverPool : Main.getServerPools()) {
                if (!server.getName().startsWith(serverPool.getType().toString())) continue;
                if (serverPool == GameType.AFK.getPool() && serverPool.getServerInstances().size() <= 1) return;

                for (ServerInstance serverInstance : serverPool.getServerInstances()) {
                    if (serverInstance.getInfos().getSocketAddress().equals(server.getSocketAddress())) {
                        if (serverInstance.getWaitingPlayers().size() + serverInstance.getInfos().getPlayers().size() > 1) {
                            serverInstance.getWaitingPlayers().remove(gamePlayer);
                            return;
                        }
                        serverPool.getServerInstances().remove(serverInstance);
                        serverInstance.remove();
                        return;
                    }
                }
            }
        });
    }

}