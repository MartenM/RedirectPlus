package nl.martenm.redirectplus.objects;

import nl.martenm.redirectplus.RedirectPlus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An class that detects problems and attempts to notify server owners.
 * These problems are all the result from misconfigurations.
 * @author MartenM
 * @since 18-5-2019.
 */
public class ConfigurationHelper {

    private RedirectPlus plugin;

    private List<ConfigurationError> errors = new ArrayList<>();
    private boolean fatalError = false;

    public ConfigurationHelper(RedirectPlus plugin) {
        this.plugin = plugin;
    }

    /**
     * Detect any loops in the parent groups.
     */
    public void runLoopCheck() {
        Map<ServerGroup, Integer> visited = new HashMap();
        plugin.getServerGroups().stream().forEach(serverGroup -> visited.put(serverGroup, 0));


        int id = 1;
        for(ServerGroup serverGroup : plugin.getServerGroups()) {
            ServerGroup current = serverGroup;

            while (current.getParent() != null) {
                // Mark current as visited.
                visited.put(current, id);

                ServerGroup next = current.getParent();
                if(visited.get(next) == id) {
                    // Loop detected. Unlink and notify.
                    registerError(new String[] {
                            "An loop was detected in your configuration of parent groups.",
                            "Please change the parent group of '" + next.getName() + "' so that there are no loops."},
                            true);
                }

                current = next;
            }

            id++;
        }
    }

    /**
     * Detect if any server group has the same alias as another one.
     * Non fatal if this occurs but better to notify the server owners.
     */
    public void runAliasCheck() {
        Map<String, ServerGroup> aliases = new HashMap<>();
        for(ServerGroup serverGroup : plugin.getServerGroups()) {
            for(String alias : serverGroup.getAliases()) {
                if(aliases.containsKey(alias)) {
                    // We don't undo this, since it's not really creating any errors, but we notify the server
                    // owner anyway.

                    registerError(new String[] {
                            "Found server groups that share the same alias: '" + alias + "'.",
                            "Server groups: '" + aliases.get(alias).getName() + "' and '" + serverGroup.getName() + "'"
                    }, false);
                } else {
                    // Add the new alias to the list.
                    aliases.put(alias, serverGroup);
                }
            }
        }
    }

    private void registerError(String[] messages, boolean fatal) {
        if(fatal) {
            this.fatalError = true;
        }

        this.errors.add(new ConfigurationError(messages, fatal));
    }

    public void printErrors() {
        plugin.getLogger().warning("==================[ Redirect Plus - Configuration Error ]====================");
        plugin.getLogger().warning("      There is an error in your configuration, please solve this error asp:");
        plugin.getLogger().warning(" ");

        // Print all errors.
        for(ConfigurationError error : errors) {
            if(error.isFatal()) {
                plugin.getLogger().info("FATAL:   (this error will disable the plugin)");
            }

            for(String m : error.getMessages()) {
                plugin.getLogger().warning(m);
            }

            plugin.getLogger().info(" ");
        }

        if(fatalError) {
            plugin.getLogger().warning("                                FATAL");
            plugin.getLogger().warning("             The error prevented the plugin from loading.");
            plugin.getLogger().warning("        Please fix the error and restart your BungeeCord server.");
            plugin.getLogger().warning(" ");
        }

        plugin.getLogger().warning("==============================================================================");
    }


    public boolean isFatalError() {
        return fatalError;
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }
}
