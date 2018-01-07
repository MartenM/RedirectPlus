package nl.martenm.redirect.objects;

import net.md_5.bungee.api.config.ServerInfo;

/**
 * @author MartenM
 * @since 6-1-2018.
 */
public class PriorityWrapper {

    private ServerInfo serverInfo;
    private int priority;
    private boolean isOnline;

    public PriorityWrapper(ServerInfo serverInfo, int priority){
        this.serverInfo = serverInfo;
        this.priority = priority;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
