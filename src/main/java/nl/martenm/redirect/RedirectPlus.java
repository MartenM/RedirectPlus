package nl.martenm.redirect;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import nl.martenm.redirect.commands.RedirectCommand;
import nl.martenm.redirect.listeners.PlayerKickListener;
import nl.martenm.redirect.metrics.Metrics;
import nl.martenm.redirect.objects.PriorityWrapper;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author MartenM
 * @since 5-1-2018.
 */
public class RedirectPlus extends Plugin {

    private Configuration config;
    private List<PriorityWrapper> servers;
    private ScheduledTask checker;
    private Metrics metrics = null;

    @Override
    public void onEnable() {

        servers = new ArrayList<>();

        getLogger().info("Registering config...");
        registerConfig();

        getLogger().info("Creating event listeners...");
        registerEvents();

        getLogger().info("Registering commands...");
        registerCommands();

        getLogger().info("Doing magic stuff so that the plugin will work...");
        setup();

        getLogger().info("Creating bStats.org metrics...");
        metrics = new Metrics(this);

        getLogger().info("Successfully enabled Redirect Plus.");
    }

    @Override
    public void onDisable() {
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
    }

    private void setup(){
        if(servers == null){
            servers = new ArrayList<>();
        } else servers.clear();

        for(String key : config.getSection("servers").getKeys()) {
            ServerInfo info = getProxy().getServerInfo(key.replace("%", "."));
            if (info == null) {
                getLogger().warning("Failed to find the server: " + key);
                continue;
            }
            servers.add(new PriorityWrapper(info, config.getInt("servers." + key + ".priority")));
        }

        servers.sort((o1, o2) ->
                (o1.getPriority() > o2.getPriority() ? 1 : 0));

        checker = getProxy().getScheduler().schedule(this, () -> {
            for (PriorityWrapper server : servers) {
                ServerInfo info = server.getServerInfo();

                if (info == null) {
                    continue;
                }

                info.ping((serverPing, throwable) -> {
                    getProxy().getScheduler().schedule(this, () -> {
                        server.setOnline(throwable == null);
                    }, 1, TimeUnit.MILLISECONDS);
                });
            }
        }, 0, config.getInt("check"), TimeUnit.SECONDS);
    }

    public Configuration getConfig() {
        return config;
    }

    public List<PriorityWrapper> getOnlineServer(){
        return servers.stream().filter(PriorityWrapper::isOnline).collect(Collectors.toList());
    }

    public List<PriorityWrapper> getOfflineServer(){
        return servers.stream().filter(s -> !s.isOnline()).collect(Collectors.toList());
    }

    public List<PriorityWrapper> getAllServers(){
        return servers;
    }

    public boolean updateServer(String name, Boolean online){
        for(PriorityWrapper wrapper : servers){
            if(wrapper.getServerInfo().getName().equalsIgnoreCase(name)){
                wrapper.setOnline(online);
                return true;
            }
        }
        return false;
    }

    public void reload(){
        registerConfig();
        setup();
        getLogger().info("Reload completed.");
    }
}
