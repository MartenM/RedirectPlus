package nl.martenm.redirectplus.objects;

import net.md_5.bungee.api.config.ServerInfo;

/**
 * The <strong>RedirectServerWrapper</strong> holds information about redirect servers.
 * These instances only made once (when the plugin is loaded).
 * @author MartenM
 * @since 6-1-2018.
 */
public class RedirectServerWrapper {

    private ServerInfo serverInfo;
    private boolean isOnline;
    private ServerGroup serverGroup;
    private boolean redirectable;
    private int onlinePlayersCount;

    private boolean allowAliases = true;

    public RedirectServerWrapper(ServerInfo serverInfo){
        this(serverInfo, null, false);
    }

    public RedirectServerWrapper(ServerInfo serverInfo, ServerGroup serverGroup, boolean redirectable) {
        this.serverInfo = serverInfo;
        this.serverGroup = serverGroup;
        this.redirectable = redirectable;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public ServerGroup getServerGroup() {
        return serverGroup;
    }

    /**
     * Whether this server should receive redirects.
     * @return
     */
    public boolean isRedirectable() {
        return redirectable;
    }

    /**
     * The estimated player count.
     * This is an estimate because it is only updated once in the X seconds.
     * @return
     */
    public int getOnlinePlayersCount() {
        return onlinePlayersCount;
    }

    public void setOnlinePlayersCount(int onlinePlayersCount) {
        this.onlinePlayersCount = onlinePlayersCount;
    }

    /**
     * Adds an proxied player to the player count so a better approximation is made.
     */
    public void addProxiedPlayer() {
        onlinePlayersCount++;
    }

    /**
     * Remove an proxied player from the player count so a better approximation is made.
     */
    public void removeProxiedPlayer() {
        onlinePlayersCount--;
    }

    /**
     * Set if people should be able to use aliases on this server.
     * @return
     */
    public boolean isAllowAliases() {
        return allowAliases;
    }

    
    public void setAllowAliases(boolean allowAliases) {
        this.allowAliases = allowAliases;
    }
}
