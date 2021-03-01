package nl.martenm.redirectplus;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import nl.martenm.redirectplus.api.events.RedirectServerStatusChangeEvent;
import nl.martenm.redirectplus.commands.RedirectCommand;
import nl.martenm.redirectplus.enums.SpreadMode;
import nl.martenm.redirectplus.listeners.ChatEventListener;
import nl.martenm.redirectplus.listeners.PlayerKickListener;
import nl.martenm.redirectplus.listeners.channels.MessageListenerExecuteAlias;
import nl.martenm.redirectplus.metrics.Metrics;
import nl.martenm.redirectplus.objects.ConfigurationHelper;
import nl.martenm.redirectplus.objects.RedirectServerWrapper;
import nl.martenm.redirectplus.objects.ServerGroup;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author MartenM
 * @since 5-1-2018.
 */
public class RedirectPlus extends Plugin {

    public static final String CHANNEL_NAME = "martenm:redirectplus";

    private Configuration config;
    private Map<String, RedirectServerWrapper> servers;
    private List<ServerGroup> serverGroups;
    private ScheduledTask checker;
    private Metrics metrics = null;
    private boolean disabled = false;

    @Override
    public void onEnable() {

        servers = new HashMap<>();
        serverGroups = new ArrayList<>();

        getLogger().info("Registering config...");
        registerConfig();

        getLogger().info("Registering commands...");
        registerCommands();

        getLogger().info("Doing magic stuff so that the plugin will work...");
        setup();

        getLogger().info("Running some checks to make sure you did not mess up :)");
        ConfigurationHelper configurationHelper = new ConfigurationHelper(this);
        configurationHelper.runLoopCheck();
        configurationHelper.runAliasCheck();

        // Print errors if there are any.
        if(configurationHelper.hasErrors())
            configurationHelper.printErrors();

        // Check if the plugin can safely be enabled or if action should be undertaken before we allow it.
        if(configurationHelper.isFatalError()) {
            this.disabled = true;
            getProxy().getPluginManager().unregisterListeners(this);
            return;
        }

        getLogger().info("Creating event listeners...");
        registerEvents();

        getLogger().info("Registering plugin message channel...");
        getProxy().registerChannel(CHANNEL_NAME);


        getLogger().info("Creating bStats.org metrics...");
        metrics = new Metrics(this, 2095);

        getLogger().info("Successfully enabled Redirect Plus.");
    }

    @Override
    public void onDisable() {
        this.disabled = true;
        checker.cancel();
        getLogger().info("Disabling. Thanks for using the plugin!");
    }

    private void registerCommands(){
        PluginManager manager = getProxy().getPluginManager();
        manager.registerCommand(this, new RedirectCommand(this));
    }

    private void registerConfig(){
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerEvents(){
        getProxy().getPluginManager().registerListener(this, new PlayerKickListener(this));
        getProxy().getPluginManager().registerListener(this, new ChatEventListener(this));
        getProxy().getPluginManager().registerListener(this, new MessageListenerExecuteAlias(this));
    }

    /**
     * Creates the default task.
     * Can also be used to reload the plugin.
     */
    private void setup(){
        if(servers == null){
            servers = new HashMap<>();
        } else servers.clear();

        // Fetch server groups and servers connected to them.

        // Fetch servers
        for(String key : config.getSection("groups").getKeys()) {

            SpreadMode spreadMode = null;
            int minimalProgressive = 0;

            if(getConfig().get("groups." + key + ".spread-mode") != null) {
                spreadMode = SpreadMode.valueOf(getConfig().getString("groups." + key + ".spread-mode"));
            }
            if(getConfig().get("groups." + key + ".progressive-minimal") != null) {
                minimalProgressive = getConfig().getInt("groups." + key + ".progressive-minimal");
            }

            ServerGroup serverGroup = new ServerGroup(
                    this,
                    key,
                    config.getBoolean("groups." + key + ".bottom-kick"),
                    config.getBoolean("groups." + key + ".spread"),
                    config.getString("groups." + key + ".parent-group"),
                    config.getStringList("groups." + key + ".aliases"),
                    spreadMode,
                    minimalProgressive,
                    config.getString("groups." + key + ".permission")
            );

            for(String servername : config.getStringList("groups." + key + ".servers")) {
                ServerInfo info = getProxy().getServerInfo(servername.replace("%", "."));
                if (info == null) {
                    getLogger().warning("Failed to find the server: " + key + "(PATH: groups." + key + "servers > " + servername);
                    continue;
                }

                // Found server, add it to the global server list to allow easy ping.
                RedirectServerWrapper redirectServerWrapper = new RedirectServerWrapper(info, serverGroup, true);
                if(!servers.containsKey(servername))
                    servers.put(servername, redirectServerWrapper);

                serverGroup.addServer(redirectServerWrapper);
            }

            // server regex
            if(!config.getString("groups." + key + ".servers-regex").equalsIgnoreCase("") && !config.getString("group." + key + ".servers-regex").equalsIgnoreCase("none")) {
                String regexString = config.getString("groups." + key + ".servers-regex");

                for(Map.Entry<String, ServerInfo> entry : getProxy().getServers().entrySet()) {
                    // If server name does not match the regex string we continue.
                    if(!entry.getKey().matches(regexString)) {
                        continue;
                    }

                    // Otherwise, add it do the servergroup.
                    // Found server, add it to the global server list to allow easy ping.
                    String servername = entry.getKey();
                    if(servers.containsKey(servername)) {
                        // To prevent and to be more flexible we ignore servers that have already been assigned a server group.
                        // We do notify about this in the console though.
                        getLogger().info(String.format("Server %s matched the regex of %s but is not added due to already being assigned a server group.", servername, serverGroup.getName()));
                        continue;
                    }

                    RedirectServerWrapper redirectServerWrapper = new RedirectServerWrapper(entry.getValue(), serverGroup, true);
                    servers.put(servername, redirectServerWrapper);

                    serverGroup.addServer(redirectServerWrapper);
                }
            }

            for(String servername : config.getStringList("groups." + key + ".connected")) {
                ServerInfo info = getProxy().getServerInfo(servername.replace("%", "."));
                if (info == null) {
                    getLogger().warning("Failed to find the server: " + key + "(PATH: groups." + key + "servers > " + servername);
                    continue;
                }

                // Found server, add it to the global server list to allow easy ping.
                RedirectServerWrapper redirectServerWrapper = new RedirectServerWrapper(info, serverGroup, false);
                servers.put(servername, redirectServerWrapper);

                if(!servers.containsKey(servername))
                    servers.put(servername, redirectServerWrapper);

                serverGroup.addConnectedServer(redirectServerWrapper);
            }

            // Connected regex
            if(!config.getString("groups." + key + ".connected-regex").equalsIgnoreCase("none")) {
                String regexString = config.getString("groups." + key + ".connected-regex");

                for(Map.Entry<String, ServerInfo> entry : getProxy().getServers().entrySet()) {
                    // If server name does not match the regex string we continue.
                    if(!entry.getKey().matches(regexString)) {
                        continue;
                    }

                    // Otherwise, add it do the servergroup.
                    // Found server, add it to the global server list to allow easy ping.
                    String servername = entry.getKey();
                    if(servers.containsKey(servername)) {
                        // To prevent and to be more flexible we ignore servers that have already been assigned a server group.
                        // We do notify about this in the console though.
                        getLogger().info(String.format("Server %s matched the regex of %s but is not added due to already being assigned a server group.", servername, serverGroup.getName()));
                        continue;
                    }

                    RedirectServerWrapper redirectServerWrapper = new RedirectServerWrapper(entry.getValue(), serverGroup, true);
                    servers.put(servername, redirectServerWrapper);

                    serverGroup.addConnectedServer(redirectServerWrapper);
                }
            }

            serverGroups.add(serverGroup);
        }

        // For each server check if they have aliases disabled.
        if (getConfig().getSection("disable-aliases") != null) {
            Configuration settings = getConfig().getSection("disable-aliases");

            for (String serverName : settings.getStringList("servers")) {
                RedirectServerWrapper server = servers.get(serverName);
                ServerInfo info = getProxy().getServerInfo(serverName);

                if (info == null) {
                    getLogger().warning(String.format("The server %s was not found when attempting to disable aliases.", serverName));
                    continue;
                }

                if (server == null) {
                    server = new RedirectServerWrapper(info);
                    servers.put(serverName, server);
                    continue;
                }

                server.setAllowAliases(false);
            }


            String regex = settings.getString("regex");
            if (!regex.equalsIgnoreCase("none")) {
                for (RedirectServerWrapper server : servers.values()) {
                    if (server.getServerInfo().getName().matches(regex)) {
                        server.setAllowAliases(false);
                    }
                }
            }
        }

        // Start checker task to see if servers are online / offline
        if(checker != null){
            checker.cancel();
        }

        checker = getProxy().getScheduler().schedule(this, () -> {
            updateServers();
        }, 0, config.getInt("check"), TimeUnit.SECONDS);
    }

    public void updateServers() {
        for (RedirectServerWrapper server : servers.values()) {
            ServerInfo info = server.getServerInfo();

            if (info == null) {
                continue;
            }

            info.ping((serverPing, throwable) -> {
                // Don't update and schedule when the plugin has been disabled.
                // This will cause errors to be thrown.
                if(this.disabled) {
                    return;
                }

                getProxy().getScheduler().schedule(this, () -> {

                    // Get old data and populate the event.
                    RedirectServerStatusChangeEvent apiEvent = new RedirectServerStatusChangeEvent(server, server.getOnlinePlayersCount(), server.isOnline());

                    if(throwable == null) {
                        server.setOnline(true);
                        server.setOnlinePlayersCount(info.getPlayers().size());
                    } else {
                        server.setOnline(false);
                        server.setOnlinePlayersCount(0);
                    }

                    // Call the event
                    getProxy().getPluginManager().callEvent(apiEvent);

                }, 1, TimeUnit.MILLISECONDS);
            });
        }
    }

    public Configuration getConfig() {
        return config;
    }

    /**
     * Remote void to change the server status of a server.
     * @return true on success.
     */
    public boolean updateServer(String name, Boolean online){
        if(servers.containsKey(name)) {
            servers.get(name).setOnline(online);
            return true;
        } return false;
    }

    /**
     * Method used to reload the plugin.
     */
    public void reload(){
        serverGroups.clear();
        servers.clear();

        registerConfig();
        setup();
        getLogger().info("Reload completed.");
    }

    public ServerGroup getServerGroup(String name){
        for(ServerGroup serverGroup : serverGroups) {
            if(serverGroup.getName().equalsIgnoreCase(name)) {
                return serverGroup;
            }
        }
        return null;
    }

    public ServerGroup getUnkownServerGroup() {
        return getServerGroup(getConfig().getString("unknown-group"));
    }

    public RedirectServerWrapper getServer(String name) {
        if(servers.containsKey(name)) {
            return servers.get(name);
        } return null;
    }

    public List<RedirectServerWrapper> getOnlineServers(){
        return new ArrayList<>(servers.values().stream().filter(server -> server.isOnline()).collect(Collectors.toList()));
    }

    public List<RedirectServerWrapper> getOfflineServers(){
        return new ArrayList<>(servers.values().stream().filter(server -> !server.isOnline()).collect(Collectors.toList()));
    }

    public List<RedirectServerWrapper> getRedirectServers() {
        return new ArrayList<>(servers.values());
    }

    public List<ServerGroup> getServerGroups() {
        return serverGroups;
    }

    public boolean isDisabled() {
        return disabled;
    }
}
