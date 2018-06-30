package nl.martenm.redirectplus.api.events;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * The <strong>ProxiedPlayerRedirectEvent</strong> is fired when a (Proxied) player is redirected after a server kick.
 * Please note that this only happens when an actual redirect about to happens.
 * @author MartenM
 * @since 30-6-2018.
 */
public class ProxiedPlayerRedirectEvent extends Event implements Cancellable {

    private boolean cancelled = false;
    private ProxiedPlayer player;
    private ServerInfo from;
    private ServerInfo to;
    private String kickMessage;

    public ProxiedPlayerRedirectEvent(ProxiedPlayer player, ServerInfo from, ServerInfo to, String kickMessage) {
        this.player = player;
        this.from = from;
        this.to = to;
        this.kickMessage = kickMessage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public ServerInfo getFrom() {
        return from;
    }

    public ServerInfo getTo() {
        return to;
    }

    public String getKickMessage() {
        return kickMessage;
    }
}
