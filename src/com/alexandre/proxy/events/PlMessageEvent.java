package com.alexandre.proxy.events;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.alexandre.proxy.GameType;
import com.alexandre.proxy.Main;
import com.alexandre.proxy.ServerInstance;
import com.alexandre.proxy.ServerPool;
import com.alexandre.proxy.players.GamePlayer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;

public class PlMessageEvent implements Listener {

	@EventHandler
	public void onPluginMessage(PluginMessageEvent event) {
		if (!event.getTag().equalsIgnoreCase("CoreServer")) return;
		if (event.getReceiver() instanceof Server) return;

		ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
		String subChannel = in.readUTF();
		if (subChannel.equalsIgnoreCase("JoinGame")) {
			GameType type = GameType.valueOf(in.readUTF());
			ProxiedPlayer receiver = (ProxiedPlayer) event.getReceiver();

			GamePlayer player = Main.getPlayer(receiver);
			if (player == null) return;

			Server server = (Server) event.getSender();

			ServerInstance serverInstance = type.getPool().getRandomServer(receiver, false);
			if (serverInstance.isActive()) player.getPlayer().connect(serverInstance.getInfos());
			else {
				player.getPlayer().sendMessage(new TextComponent("§aJoining " + type));
			}
		}

		if (subChannel.equalsIgnoreCase("CanJoin")) {
			Server server = (Server) event.getSender();
			if (server == null) return;
			for (ServerPool serverPool : Main.getServerPools()) {
				if (!server.getInfo().getName().startsWith(serverPool.getType().toString())) continue;

				for (ServerInstance serverInstance : serverPool.getServerInstances()) {
					if (serverInstance.getInfos().getSocketAddress().equals(server.getInfo().getSocketAddress())) {
						serverInstance.setCanJoin(in.readBoolean());
						return;
					}
				}
			}
		}

		if (subChannel.equalsIgnoreCase("Close")) {
			Server server = (Server) event.getSender();
			if (server == null) return;
			for (ServerPool serverPool : Main.getServerPools()) {
				if (!server.getInfo().getName().startsWith(serverPool.getType().toString())) continue;

				for (ServerInstance serverInstance : new ArrayList<>(serverPool.getServerInstances())) {
					if (serverInstance.getInfos().getSocketAddress().equals(server.getInfo().getSocketAddress())) {
						serverPool.getServerInstances().remove(serverInstance);
						serverInstance.remove();
						return;
					}
				}
			}
		}
	}
}