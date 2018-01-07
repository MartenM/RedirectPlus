package nl.martenm.redirect.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.chat.BaseComponentSerializer;
import net.md_5.bungee.event.EventHandler;
import nl.martenm.redirect.RedirectPlus;
import nl.martenm.redirect.objects.PriorityWrapper;

/**
 * @author MartenM
 * @since 5-1-2018.
 */
public class PlayerKickListener implements Listener {

    private RedirectPlus plugin;

    public PlayerKickListener(RedirectPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleKickEvent(ServerKickEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (plugin.getOnlineServer().size() < 1) {
            return;
        }

        ServerInfo kickedFrom = event.getKickedFrom();
        ServerInfo kickedTo = plugin.getOnlineServer().get(0).getServerInfo();

        // Blacklist
        for(String word : plugin.getConfig().getStringList("blacklist")) {
            if (BaseComponent.toLegacyText(event.getKickReasonComponent()).contains(word))
                return;
        }

        // Detect shutdown.
        if(plugin.getConfig().getBoolean("detect-shutdown")){
            for(String word : plugin.getConfig().getStringList("detect-messages")){
                if(BaseComponent.toLegacyText(event.getKickReasonComponent()).contains(word)){
                    plugin.updateServer(kickedFrom.getName(), false);
                    break;
                }
            }
        }

        // BottomKick
        if(plugin.getConfig().getBoolean("bottom-kick")){
           if(plugin.getAllServers().stream().filter(server -> server.getServerInfo().getName().equalsIgnoreCase(kickedFrom.getName())).count() > 0 ){
               return;
           }
        }

        // KickedFrom == Kicked to. No change needed.
        if (kickedFrom == kickedTo) {
            boolean changed = false;
            for(PriorityWrapper wrapper : plugin.getOnlineServer()){
                if(wrapper.getServerInfo() == kickedTo){
                    continue;
                }
                kickedTo = wrapper.getServerInfo();
                changed = true;
                break;
            }

            if(!changed)
                return;
        }

        event.setCancelled(true);
        event.setCancelServer(kickedTo);

        for (String message : plugin.getConfig().getStringList("messages.redirected")) {
            message = ChatColor.translateAlternateColorCodes('&', message.replace("%reason%", BaseComponent.toLegacyText(event.getKickReasonComponent())));
            player.sendMessage(new ComponentBuilder(message).create());
        }
    }
}
