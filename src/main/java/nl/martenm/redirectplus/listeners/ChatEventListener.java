package nl.martenm.redirectplus.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import nl.martenm.redirectplus.RedirectPlus;
import nl.martenm.redirectplus.api.events.ProxiedPlayerGroupAliasExecuted;
import nl.martenm.redirectplus.objects.RedirectServerWrapper;
import nl.martenm.redirectplus.objects.ServerGroup;

import java.util.Arrays;

/**
 * @author MartenM
 * @since 22-6-2018.
 */
public class ChatEventListener implements Listener {

    private RedirectPlus plugin;
    public ChatEventListener (RedirectPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleChatEvent(ChatEvent event) {
        
        if(event.isCancelled()) return;

        if(!event.getMessage().startsWith("/")) {
            return;
        }

        String[] args = event.getMessage().substring(1).split(" ");
        if(args.length == 0) {
            return;
        }

        if(!(event.getSender() instanceof ProxiedPlayer)) {
            // Only console player senders.
            return;
        }

        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) event.getSender();
        event.setCancelled(handleAliasExecution(plugin, args[0], proxiedPlayer));
    }

    public static boolean handleAliasExecution(RedirectPlus plugin, String command, ProxiedPlayer proxiedPlayer) {
        ServerGroup serverGroup = null;
        for(ServerGroup sg : plugin.getServerGroups()) {
            if(Arrays.stream(sg.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(command))) {
                serverGroup = sg;
                break;
            }
        }

        if(serverGroup == null) {
            return false;
        }

        if(serverGroup.isRestricted()) {
            if(!proxiedPlayer.hasPermission(serverGroup.getPermission())) {
                for (String message : plugin.getConfig().getStringList("messages.alias-no-permission")) {
                    message = ChatColor.translateAlternateColorCodes('&', message);
                    proxiedPlayer.sendMessage(TextComponent.fromLegacyText(message));
                }
                return true;
            }
        }

        ServerInfo currentServer = proxiedPlayer.getServer().getInfo();
        ServerGroup currentServerGroup = null;
        RedirectServerWrapper currentServerWrapper = plugin.getServer(currentServer.getName());

        // Check if the player is in the same server group as the destination
        // And check if the alias exection is allowed by the server.
        if(currentServerWrapper != null) {

            if (!currentServerWrapper.isAllowAliases()) {
                for (String message : plugin.getConfig().getStringList("messages.alias-not-allowed-server")) {
                    message = ChatColor.translateAlternateColorCodes('&', message);
                    proxiedPlayer.sendMessage(TextComponent.fromLegacyText(message));
                }
                return true;
            }

            currentServerGroup = currentServerWrapper.getServerGroup();
            if(serverGroup == currentServerGroup && serverGroup.getAvailableServersSize() <= 1) {
                if(serverGroup.getServers().get(0) == currentServerWrapper || serverGroup.getAvailableServersSize() == 0) {
                    for (String message : plugin.getConfig().getStringList("messages.unable-redirect-alias-same-category")) {
                        message = ChatColor.translateAlternateColorCodes('&', message);
                        proxiedPlayer.sendMessage(TextComponent.fromLegacyText(message));
                    }
                    return true;
                }
            }
        }

        RedirectServerWrapper server = serverGroup.getRedirectServer(proxiedPlayer, proxiedPlayer.getServer().getInfo().getName(), false, serverGroup.getSpreadMode());

        if(server == null) {
            for (String message : plugin.getConfig().getStringList("messages.unable-redirect-alias")) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                proxiedPlayer.sendMessage(TextComponent.fromLegacyText(message));
            }
            return true;
        }

        // Redirect API
        ProxiedPlayerGroupAliasExecuted apiEvent = new ProxiedPlayerGroupAliasExecuted(proxiedPlayer, command, currentServerGroup, server);
        plugin.getProxy().getPluginManager().callEvent(apiEvent);

        if(apiEvent.isCancelled()) {
            return false;
        }
        //

        proxiedPlayer.connect(server.getServerInfo());
        server.addProxiedPlayer();
        if(currentServerWrapper != null) {
            currentServerWrapper.removeProxiedPlayer();
        }

        return true;
    }

}
