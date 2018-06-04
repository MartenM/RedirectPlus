package nl.martenm.redirect.objects;

import nl.martenm.redirect.RedirectPlus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
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

    public ServerGroup(RedirectPlus redirectPlus, String name, boolean bottomKick, boolean spread, String parent) {
        this.redirectPlus = redirectPlus;
        this.name = name;
        this.bottomKick = bottomKick;
        this.spread = spread;
        this.parent = parent;
        if(this.parent.equalsIgnoreCase("none"))
            parent = null;

        this.servers = new ArrayList<>();
        this.connected = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<RedirectServerWrapper> getServers() {
        return servers;
    }

    public List<RedirectServerWrapper> getConnected() {
        return connected;
    }

    public boolean isBottomKick() {
        return bottomKick;
    }

    public void setBottomKick(boolean bottomKick) {
        this.bottomKick = bottomKick;
    }

    public boolean isSpread() {
        return spread;
    }

    public void setSpread(boolean spread) {
        this.spread = spread;
    }

    public void addServer(RedirectServerWrapper server) {
        this.servers.add(server);
    }

    public void addConnectedServer(RedirectServerWrapper server) {
        this.connected.add(server);
    }

    public ServerGroup getParent() {
        return this.redirectPlus.getServerGroup(parent);
    }

    public RedirectServerWrapper getRedirectServer(String oldServer) {
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
            if(parent == null) return null;

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
}
