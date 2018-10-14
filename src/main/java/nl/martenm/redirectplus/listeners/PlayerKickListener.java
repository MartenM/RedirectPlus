package nl.martenm.redirectplus.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import nl.martenm.redirectplus.RedirectPlus;
import nl.martenm.redirectplus.api.events.ProxiedPlayerRedirectEvent;
import nl.martenm.redirectplus.objects.RedirectServerWrapper;
import nl.martenm.redirectplus.objects.ServerGroup;

import java.util.concurrent.TimeUnit;

/**
 * @author MartenM
 * @since 5-1-2018.
 */
public class PlayerKickListener implements Listener {

    private final RedirectPlus plugin;

    public PlayerKickListener(RedirectPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleKickEvent(ServerKickEvent event) {
        ProxiedPlayer player = event.getPlayer();

        ServerInfo kickedFrom = event.getKickedFrom();

        if(!event.getPlayer().isConnected()) {
            if(plugin.getConfig().getBoolean("log.inactive")) {
                plugin.getLogger().info("Cancelled the redirect of " + player.getName() + " [No longer connected]");
            }
            return;
        }

        // Blacklist
        for(String word : plugin.getConfig().getStringList("blacklist")) {
            if (BaseComponent.toLegacyText(event.getKickReasonComponent()).contains(word)) {
                if(plugin.getConfig().getBoolean("log.blacklist"))
                    plugin.getLogger().info("Cancelled the redirect of " + player.getName() + " [Blacklist: " + word + "]");
                return;
            }
        }

        // Detect shutdown.
        if(plugin.getConfig().getBoolean("detect-shutdown.enabled")){
            for(String word : plugin.getConfig().getStringList("detect-shutdown.messages")){
                if(BaseComponent.toLegacyText(event.getKickReasonComponent()).contains(word)){
                    plugin.updateServer(kickedFrom.getName(), false);
                    break;
                }
            }
        }

        // Get the server group etccc
        RedirectServerWrapper redirectServerWrapper = plugin.getServer(kickedFrom.getName());
        ServerGroup serverGroup = null;
        if(redirectServerWrapper != null) {
            serverGroup = redirectServerWrapper.getServerGroup();
        } else serverGroup = plugin.getUnkownServerGroup();

        if(serverGroup.isBottomKick()) {
            if(plugin.getConfig().getBoolean("log.bottom-kick"))
                plugin.getLogger().info("Cancelled the redirect of " + player.getName() + " [Bottom-kick]");
            return;
        }

        RedirectServerWrapper targetServer = serverGroup.getRedirectServer(kickedFrom.getName(), true, serverGroup.getSpreadMode());
        if(targetServer == null) {
            if(plugin.getConfig().getBoolean("log.redirectplus-failed"))
                plugin.getLogger().info("Redirect of " + player.getName() + " failed. [No server found]");
            return;
        }

        // Redirect API
        ProxiedPlayerRedirectEvent apiEvent = new ProxiedPlayerRedirectEvent(event.getPlayer(), event.getKickedFrom(), targetServer.getServerInfo(), event.getKickReasonComponent().toString());
        plugin.getProxy().getPluginManager().callEvent(apiEvent);

        if(apiEvent.isCancelled()) {
            return;
        }
        //

        event.setCancelled(true);
        event.setCancelServer(targetServer.getServerInfo());

        boolean hideMessage = false;
        for(String word : plugin.getConfig().getStringList("no-messages")){
            if(BaseComponent.toLegacyText(event.getKickReasonComponent()).contains(word)){
                hideMessage = true;
                break;
            }
        }

        if(!hideMessage) {
            // Schedule the for loop that sends messages so a delay can be added if needed.
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                for (String message : plugin.getConfig().getStringList("messages.redirected")) {
                    message = ChatColor.translateAlternateColorCodes('&', message.replace("%reason%", BaseComponent.toLegacyText(event.getKickReasonComponent())));
                    player.sendMessage(new ComponentBuilder(message).create());
                }
            }, plugin.getConfig().getInt("delay"), TimeUnit.SECONDS);
        }

        if(plugin.getConfig().getBoolean("log.redirected"))
            plugin.getLogger().info("Redirected " + player.getName() + " from " + event.getKickedFrom().getName() + " to " + targetServer.getServerInfo().getName());
        targetServer.addProxiedPlayer();
        if(redirectServerWrapper != null)
            redirectServerWrapper.removeProxiedPlayer();
    }
}
