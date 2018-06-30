package nl.martenm.redirectplus.api.events;

import net.md_5.bungee.api.plugin.Event;
import nl.martenm.redirectplus.objects.RedirectServerWrapper;

/**
 * The <strong>RedirectServerStatusChangeEvent</strong> is fired when a server has been pinged and a status has been received.
 * @author MartenM
 * @since 30-6-2018.
 */
public class RedirectServerStatusChangeEvent extends Event {

    private RedirectServerWrapper server;
    private int oldPlayerCount;
    private boolean oldOnline;

    public RedirectServerStatusChangeEvent(RedirectServerWrapper server, int oldPlayerCount, boolean oldOnline) {
        this.server = server;
        this.oldPlayerCount = oldPlayerCount;
        this.oldOnline = oldOnline;
    }

    public RedirectServerWrapper getServer() {
        return server;
    }

    public int getOldPlayerCount() {
        return oldPlayerCount;
    }

    public boolean isOldOnline() {
        return oldOnline;
    }
}
