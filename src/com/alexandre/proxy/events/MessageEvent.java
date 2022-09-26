package com.alexandre.proxy.events;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Locale;

public class MessageEvent implements Listener {

    String[] commands = new String[] {"pl", "plugin", "plugins", "?", "help", "bukkit:pl", "bukkit:plugin", "bukkit:plugins"};

    @EventHandler
    public void onMessageSent(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) return;
        if (!event.getMessage().startsWith("/")) return;

        for (String command : commands) {
            if (event.getMessage().toLowerCase(Locale.ROOT).startsWith("/" + command)) {
                event.setCancelled(true);
                ((ProxiedPlayer) event.getSender()).sendMessage(new TextComponent("Unknown command. Type \"/help\" for help."));
                return;
            }
        }
    }

}