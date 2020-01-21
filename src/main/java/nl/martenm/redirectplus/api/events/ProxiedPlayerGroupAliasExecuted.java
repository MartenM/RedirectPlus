package nl.martenm.redirectplus.api.events;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;
import nl.martenm.redirectplus.objects.RedirectServerWrapper;
import nl.martenm.redirectplus.objects.ServerGroup;

/**
 * The <strong>ProxiedPlayerGroupAliasExecuted</strong> occurs when a player attempts to use an alias that redirects them to a server group.
 * Note that this event is only fired if all checks are passed and a server has been found to send the player to.
 * @author MartenM
 * @since 30-6-2018.
 */
public class ProxiedPlayerGroupAliasExecuted extends Event implements Cancellable {


    private boolean cancelled = false;
    private String alias;
    private ProxiedPlayer player;
    private ServerGroup currentServerGroup;
    private RedirectServerWrapper target;

    public ProxiedPlayerGroupAliasExecuted (ProxiedPlayer player, String alias, ServerGroup currentServerGroup, RedirectServerWrapper target) {
        this.player = player;
        this.alias = alias;
        this.currentServerGroup = currentServerGroup;
        this.target = target;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public String getAlias() {
        return alias;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public ServerGroup getCurrentServerGroup() {
        return currentServerGroup;
    }

    public RedirectServerWrapper getTarget() {
        return target;
    }
}
