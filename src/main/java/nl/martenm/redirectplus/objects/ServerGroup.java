package nl.martenm.redirectplus.objects;

import nl.martenm.redirectplus.RedirectPlus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A <strong>ServerGroup</strong> represents a list/group of servers. This includes both servers that can be redirected to and servers that cannot be redirected to.
 * There are also various methods for grabbing a server and group specific tasks.
 * @author MartenM
 * @since 4-6-2018.
 */
public class ServerGroup {

    private RedirectPlus redirectPlus;

    private String name;
    private List<RedirectServerWrapper> servers;
    private List<RedirectServerWrapper> connected;
    private String parent;
    private boolean bottomKick;
    private boolean spread;
    private int spreadCounter = 0;
    private String[] aliases;

    public ServerGroup(RedirectPlus redirectPlus, String name, boolean bottomKick, boolean spread, String parent, String[] aliases) {
        this.redirectPlus = redirectPlus;
        this.name = name;
        this.bottomKick = bottomKick;
        this.spread = spread;
        this.parent = parent;
        if(this.parent.equalsIgnoreCase("none"))
            parent = null;

        this.servers = new ArrayList<>();
        this.connected = new ArrayList<>();
        this.aliases = aliases;
    }

    public ServerGroup(RedirectPlus redirectPlus, String name, boolean bottomKick, boolean spread, String parent, List<String> aliases) {
        this(redirectPlus, name, bottomKick, spread, parent, aliases.toArray(new String[aliases.size()]));
    }

    /**
     * Get the name of the ServerGroup.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get all the servers that can be redirected to.
     * @return
     */
    public List<RedirectServerWrapper> getServers() {
        return servers;
    }

    /**
     * Get all the servers that are connected to this group, but cannot be redirected to.
     * @return
     */
    public List<RedirectServerWrapper> getConnected() {
        return connected;
    }

    /**
     * Get whether bottom-kick is enabled for this group of servers.
     * @return
     */
    public boolean isBottomKick() {
        return bottomKick;
    }

    public void setBottomKick(boolean bottomKick) {
        this.bottomKick = bottomKick;
    }

    /**
     * Check whether this ServerGroup spreads the players across servers.
     * @return
     */
    public boolean isSpread() {
        return spread;
    }

    public void setSpread(boolean spread) {
        this.spread = spread;
    }

    /**
     * Add a redirectable server to the ServerGroup.
     * @param server
     */
    public void addServer(RedirectServerWrapper server) {
        this.servers.add(server);
    }

    /**
     * Add a server that is connected to this group.
     * @param server
     */
    public void addConnectedServer(RedirectServerWrapper server) {
        this.connected.add(server);
    }

    /**
     * Get the ServerGroup parent.
     * @return
     */
    public ServerGroup getParent() {
        return this.redirectPlus.getServerGroup(parent);
    }

    /**
     * Get a new server based on the old server and parent group.
     * @param oldServer The old server the player has been kicked from.
     * @param useParent Whether we are allowed to select from the parent group.
     * @return The server to be redirected to. Null if none.
     */
    public RedirectServerWrapper getRedirectServer(String oldServer, boolean useParent) {
        RedirectServerWrapper redirectServer = null;

        // Get online servers
        List<RedirectServerWrapper> onlineServers = new ArrayList<>();

        // Filter online server and the server with the same name.
        onlineServers.addAll(
                servers.stream().filter(server -> server.isOnline() && !server.getServerInfo().getName().equalsIgnoreCase(oldServer))
                        .collect(Collectors.toList()));

        if(onlineServers.size() == 0) {
            // There are no online servers. Get the parent group or return null.
            ServerGroup parent = getParent();
            if(parent == null || !useParent) return null;

            return parent.getRedirectServer(oldServer);
        }

        if(spread) {
            spreadCounter++;
            if(spreadCounter >= onlineServers.size()){
                spreadCounter = 0;
            }

            redirectServer = onlineServers.get(spreadCounter);
        } else redirectServer = onlineServers.get(0);

        return redirectServer;
    }

    /**
     * Get a new server based on the old server and parent group.
     * @param oldServer The old server the player has been kicked from.
     * @return
     */
    public RedirectServerWrapper getRedirectServer(String oldServer) {
        return getRedirectServer(oldServer, true);
    }

    public String[] getAliases() {
        return aliases;
    }

    public int getAvailableServersSize() {
        return (int) servers.stream().filter(server -> server.isOnline()).count();
    }
}
