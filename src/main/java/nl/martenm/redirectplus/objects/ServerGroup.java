package nl.martenm.redirectplus.objects;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import nl.martenm.redirectplus.RedirectPlus;
import nl.martenm.redirectplus.enums.SpreadMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
    private String permission;

    /* Vars for spreading */
    private int spreadCounter = 0;
    private int minimalProgressive = 0;

    public ServerGroup(RedirectPlus redirectPlus, String name, boolean bottomKick, boolean spread, String parent, String[] aliases, SpreadMode spreadMode, int minimalProgressive, String permission) {
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

        if(permission.equalsIgnoreCase("")) this.permission = null;
        else this.permission = permission;
    }

    public ServerGroup(RedirectPlus redirectPlus, String name, boolean bottomKick, boolean spread, String parent, List<String> aliases, SpreadMode spreadMode, int minimalProgressive, String permission) {
        this(redirectPlus, name, bottomKick, spread, parent, aliases.toArray(new String[aliases.size()]), spreadMode, minimalProgressive, permission);
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
    public RedirectServerWrapper getRedirectServer(ProxiedPlayer player, String oldServer, boolean useParent) {
        return getRedirectServer(player, oldServer, useParent, SpreadMode.CYCLE);
    }

    /**
     * Get a new server based on the old server and parent group.
     * @param oldServer The old server the player has been kicked from.
     * @param useParent Whether we are allowed to select from the parent group.
     * @param spreadMode Spread mode that should be used to pick a server.
     * @return The server to be redirected to. Null if none.
     */
    public RedirectServerWrapper getRedirectServer(ProxiedPlayer player, String oldServer, boolean useParent, SpreadMode spreadMode) {
        RedirectServerWrapper redirectServer = null;

        // Check if this server group has a permission assigned.
        if(isRestricted()) {
            // If there is a permission check if the user has it.
            // If the user does not have the permission grab the parent server group and use that instead.
            if(!player.hasPermission(permission)) {
                // Player does not have the permission.
                ServerGroup parent = getParent();
                if(parent == null || !useParent) return null;

                return parent.getRedirectServer(player, oldServer, useParent, spreadMode);
            }
        }

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

            return parent.getRedirectServer(player, oldServer, useParent, spreadMode);
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

                return getRedirectServer(player, oldServer, useParent, SpreadMode.LOWEST);
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
     * Get the aliases for this server group.
     * @return The aliases for this server group as array.
     */
    public String[] getAliases() {
        return aliases;
    }

    /**
     * Get the amount of available servers this server group offers.
     * @return
     */
    public int getAvailableServersSize() {
        return (int) servers.stream().filter(server -> server.isOnline()).count();
    }

    /**
     * Set the parent of this server group.
     * @param parentName The parent groups name as string
     */
    public void setParent(String parentName) {
        this.parent = parentName;
    }

    /**
     * Get the spread method this server group uses to spread players around the server.
     * @return The spreadmode enum
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

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * Returns true if this section is restricted.
     * If the section is restricted getPersmission() != null.
     * @return True if restricted
     */
    public boolean isRestricted() {
        return permission != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerGroup that = (ServerGroup) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
