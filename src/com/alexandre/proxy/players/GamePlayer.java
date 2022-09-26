package com.alexandre.proxy.players;

import com.alexandre.proxy.ServerInstance;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GamePlayer {

    private final ProxiedPlayer player;
    private ServerInstance currentServer;

    public GamePlayer(ProxiedPlayer player) {
        this.player = player;
    }

    public ProxiedPlayer getPlayer() {
        return this.player;
    }

    public ServerInstance getCurrentServer() {
        return this.currentServer;
    }

    public void setCurrentServer(ServerInstance currentServer) {
        if (this.currentServer != null) this.currentServer.getWaitingPlayers().remove(this);
        this.currentServer = currentServer;
    }
}
