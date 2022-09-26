package com.alexandre.proxy;

import com.alexandre.proxy.events.JoinEvent;
import com.alexandre.proxy.events.MessageEvent;
import com.alexandre.proxy.events.PlMessageEvent;
import com.alexandre.proxy.players.GamePlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.CopyOnWriteArrayList;

public class Main extends Plugin {

    private static Main instance;

    private static CopyOnWriteArrayList<GamePlayer> players = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<ServerPool> serverPools = new CopyOnWriteArrayList<>();

    @Override
    public void onEnable() {
        Main.instance = this;
        this.getProxy().registerChannel("CoreServer");

        for (GameType gameType : GameType.values()) {
            gameType.loadPool();
        }

        getProxy().getPluginManager().registerListener(this, new JoinEvent());
        getProxy().getPluginManager().registerListener(this, new PlMessageEvent());
        getProxy().getPluginManager().registerListener(this, new MessageEvent());

        GameType.AFK.getPool().addServer(null);
    }

    @Override
    public void onDisable() {
        this.getProxy().unregisterChannel("CoreServer");
        for (ServerPool pool : Main.getServerPools()) {
            pool.closeAll();
        }
    }

    public static Main getInstance() {
        return Main.instance;
    }

    public static CopyOnWriteArrayList<ServerPool> getServerPools() {
        return Main.serverPools;
    }

    public static GamePlayer getPlayer(ProxiedPlayer player) {
        return Main.getPlayers().stream().filter(p -> p.getPlayer() == player).findAny().orElse(null);
    }

    public static CopyOnWriteArrayList<GamePlayer> getPlayers() {
        return Main.players;
    }
}
