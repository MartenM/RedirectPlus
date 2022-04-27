package nl.martenm.redirectplus.managers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.PluginManager;
import nl.martenm.redirectplus.RedirectPlus;
import nl.martenm.redirectplus.commands.AliasCommand;
import nl.martenm.redirectplus.objects.ServerGroup;

import java.util.*;

/**
 * The alias manager class manges all logic related to aliases.
 * Some of this logic is in the AliasCommand class too. This should maybe be moved to this class instead.
 */
public class AliasManager {

    private RedirectPlus plugin;
    private ProxyServer proxy;
    private Map<ServerGroup, Command> aliasCommands = new HashMap<>();

    public AliasManager(RedirectPlus plugin) {
        this.plugin = plugin;
        this.proxy = plugin.getProxy();
    }

    /**
     * Allows for alias execution without executing from the command directly.
     * @param commandText The alias being used.
     * @param proxiedPlayer The player executing the command.
     * @return If the alias command was found.
     */
    public boolean handleAliasExecution(String commandText, ProxiedPlayer proxiedPlayer) {
        ServerGroup serverGroup = null;
        for(ServerGroup sg : plugin.getServerGroups()) {
            if(Arrays.stream(sg.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(commandText))) {
                serverGroup = sg;
                break;
            }
        }

        if(serverGroup == null) return false;

        // Get the corresponding command.
        Command command = aliasCommands.get(serverGroup);
        if(command == null) return false;

        command.execute(proxiedPlayer, new String[] {commandText});
        return true;
    }

    /**
     * Register all aliases for all servergroups as commands
     * to the proxy server.
     */
    public void registerAliasCommands() {
        PluginManager manager = proxy.getPluginManager();

        for(ServerGroup group : plugin.getServerGroups()) {
            if(group.getAliases().length == 0) continue;

            String commandName = group.getAliases()[0];
            String[] aliases = {};
            if(group.getAliases().length > 1) {
                aliases = Arrays.copyOfRange(group.getAliases(), 1, group.getAliases().length);
            }

            AliasCommand command = new AliasCommand(plugin, group, commandName, group.isRestricted() ? group.getPermission() : null, aliases);
            manager.registerCommand(plugin, command);
            aliasCommands.put(group, command);
        }
    }

    /**
     * De-register any alias commands and clear the map.
     */
    public void deregisterAliasCommand() {
        PluginManager manager = proxy.getPluginManager();
        for(Command command : aliasCommands.values()) {
            manager.unregisterCommand(command);
        }
        aliasCommands.clear();
    }
}
