package nl.martenm.redirectplus.objects;

import nl.martenm.redirectplus.RedirectPlus;
import nl.martenm.redirectplus.enums.SpreadMode;

import java.util.ArrayList;
import java.util.Comparator;
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
    private SpreadMode spreadMode;
    private String[] aliases;

    /* Vars for spreading */
    private int spreadCounter = 0;
    private int minimalProgressive = 0;

    public ServerGroup(RedirectPlus redirectPlus, String name, boolean bottomKick, boolean spread, String parent, String[] aliases, SpreadMode spreadMode, int minimalProgressive) {
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

        if(spreadMode == null) {
            this.spreadMode = SpreadMode.valueOf(redirectPlus.getConfig().getString("global.spread-mode"));
            this.minimalProgressive = redirectPlus.getConfig().getInt("global.progressive-minimal");
        } else {
            this.spreadMode = spreadMode;
            this.minimalProgressive = minimalProgressive;
        }
    }

    public ServerGroup(RedirectPlus redirectPlus, String name, boolean bottomKick, boolean spread, String parent, List<String> aliases, SpreadMode spreadMode, int minimalProgressive) {
        this(redirectPlus, name, bottomKick, spread, parent, aliases.toArray(new String[aliases.size()]), spreadMode, minimalProgressive);
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
        return getRedirectServer(oldServer, useParent, SpreadMode.CYCLE);
    }

    /**
     * Get a new server based on the old server and parent group.
     * @param oldServer The old server the player has been kicked from.
     * @param useParent Whether we are allowed to select from the parent group.
     * @param spreadMode Spread mode that should be used to pick a server.
     * @return The server to be redirected to. Null if none.
     */
    public RedirectServerWrapper getRedirectServer(String oldServer, boolean useParent, SpreadMode spreadMode) {
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

            return parent.getRedirectServer(oldServer, useParent, spreadMode);
        }

        if(spread) {
            if(spreadMode == SpreadMode.CYCLE) {
                spreadCounter++;
                if (spreadCounter >= onlineServers.size()) {
                    spreadCounter = 0;
                }

                redirectServer = onlineServers.get(spreadCounter);
            }
            else if(spreadMode == SpreadMode.RANDOM) {
                redirectServer = onlineServers.get((int) (Math.random() * onlineServers.size() + 1));
            }
            else if(spreadMode == SpreadMode.PROGRESSIVE) {
                onlineServers.sort((o1, o2) -> {
                    return o1.getOnlinePlayersCount() > o2.getOnlinePlayersCount() ? -1
                            : o1.getOnlinePlayersCount() < o2.getOnlinePlayersCount() ? 1
                            : 0;
                });

                for(int i = 0; i < onlineServers.size(); i++) {
                    RedirectServerWrapper serverWrapper = onlineServers.get(i);
                    if(serverWrapper.getOnlinePlayersCount() < minimalProgressive) {
                        return serverWrapper;
                    }
                }

                return getRedirectServer(oldServer, useParent, SpreadMode.LOWEST);
            }
            else if(spreadMode == SpreadMode.HIGHEST) {
                return onlineServers.stream().max(Comparator.comparing(RedirectServerWrapper::getOnlinePlayersCount)).get();
            }
            else if(spreadMode == SpreadMode.LOWEST) {
                return onlineServers.stream().min(Comparator.comparing(RedirectServerWrapper::getOnlinePlayersCount)).get();
            }

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

    /**
     * Get the spread method this server group uses to spread players around the server.
     * @return
     */
    public SpreadMode getSpreadMode() {
        return spreadMode;
    }

    public void setSpreadMode(SpreadMode spreadMode) {
        this.spreadMode = spreadMode;
    }

    public int getMinimalProgressive() {
        return minimalProgressive;
    }

    public void setMinimalProgressive(int minimalProgressive) {
        this.minimalProgressive = minimalProgressive;
    }
}
