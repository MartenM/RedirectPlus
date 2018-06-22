package nl.martenm.redirectplus.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import nl.martenm.redirectplus.RedirectPlus;
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

        if(!event.getMessage().startsWith("/")) {
            return;
        }

        String[] args = event.getMessage().substring(1).split(" ");
        if(args.length == 0) {
            return;
        }

        ServerGroup serverGroup = null;

        for(ServerGroup sg : plugin.getServerGroups()) {
            if(Arrays.stream(sg.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(args[0]))) {
                serverGroup = sg;
                break;
            }
        }

        if(serverGroup == null) {
            return;
        }

        if(!(event.getSender() instanceof ProxiedPlayer)) {
           // Wut happend here?!
           return;
        }

        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) event.getSender();

        ServerInfo currentServer = proxiedPlayer.getServer().getInfo();
        ServerGroup currentServerGroup = plugin.getServer(currentServer.getName()).getServerGroup();

        if(serverGroup.getAvailableServersSize() <= 1 && currentServerGroup == serverGroup) {
            for (String message : plugin.getConfig().getStringList("messages.unable-redirect-alias-same-category")) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                proxiedPlayer.sendMessage(new ComponentBuilder(message).create());
            }
            event.setCancelled(true);
            return;
        }

        RedirectServerWrapper server = serverGroup.getRedirectServer(proxiedPlayer.getServer().getInfo().getName(), false);

        if(server == null) {
            for (String message : plugin.getConfig().getStringList("messages.unable-redirect-alias")) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                proxiedPlayer.sendMessage(new ComponentBuilder(message).create());
            }
            event.setCancelled(true);
            return;
        }

        proxiedPlayer.connect(server.getServerInfo());
        event.setCancelled(true);
    }


}
