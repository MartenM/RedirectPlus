package nl.martenm.redirectplus.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import nl.martenm.redirectplus.RedirectPlus;
import nl.martenm.redirectplus.objects.RedirectServerWrapper;
import nl.martenm.redirectplus.objects.ServerGroup;

/**
 * @author MartenM
 * @since 5-1-2018.
 */
public class RedirectCommand extends Command {

    private final RedirectPlus plugin;

    public RedirectCommand(RedirectPlus plugin) {
        super("redirectplus", "redirectplus.admin", "rd");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        commandSender.sendMessage(new ComponentBuilder("").create());

        String colourMode = ChatColor.GRAY.toString();
        if(!(commandSender instanceof ProxiedPlayer)) {
           colourMode = ChatColor.WHITE.toString();
        }

        if(plugin.isDisabled()) {
            sendHeader(commandSender, colourMode);
            commandSender.sendMessage(new ComponentBuilder(ChatColor.RED + "  " + "The plugin has been disabled.").create());
            commandSender.sendMessage(new ComponentBuilder(ChatColor.RED + "  " + "Please check the console for more information..").create());
            return;
        }

        if(strings.length == 0){
            sendHelp(commandSender, colourMode);
            return;
        }

        if(strings[0].equalsIgnoreCase("help")){
            sendHeader(commandSender, colourMode);
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "  " + "/rd servers").create());
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "  " + "/rd groups").create());
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "  " + "/rd refresh").create());
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "  " + "/rd reload").create());
            return;
        }

        else if(strings[0].equalsIgnoreCase("reload")){
            plugin.reload();
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "" + "Successfully reloaded the config file.").create());
            return;
        }

        else if(strings[0].equalsIgnoreCase("refresh")){
            plugin.updateServers();
            commandSender.sendMessage(new ComponentBuilder(ChatColor.YELLOW + "" + "Refreshing server info right now. Pleas note that this may take some time (1-10 seconds).").create());
            return;
        }

        else if(strings[0].equalsIgnoreCase("servers")){
            sendHeader(commandSender, colourMode);
            for(RedirectServerWrapper server : plugin.getRedirectServers())
            {
                commandSender.sendMessage(new ComponentBuilder(ChatColor.WHITE + ChatColor.BOLD.toString() + server.getServerInfo().getName() + colourMode + ": ").create());
                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Status: " + (server.isOnline() ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline")).create());
                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Group: " + ChatColor.YELLOW + server.getServerGroup().getName()).create());
                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Receives Redirects: " + (server.isRedirectable() ? ChatColor.GREEN + "Yes" : ChatColor.YELLOW + "No")).create());
                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Players (aprox): " + (server.getOnlinePlayersCount() == 0 ? ChatColor.RED.toString() + server.getOnlinePlayersCount() : ChatColor.YELLOW.toString() + server.getOnlinePlayersCount())).create());

                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Moth: " + colourMode + server.getServerInfo().getMotd()).create());
                commandSender.sendMessage(new ComponentBuilder(colourMode + " ").create());
            }
            return;
        }
        else if(strings[0].equalsIgnoreCase("groups")){
            sendHeader(commandSender, colourMode);
            for(ServerGroup serverGroup : plugin.getServerGroups())
            {
                commandSender.sendMessage(new ComponentBuilder(ChatColor.WHITE + ChatColor.BOLD.toString() + serverGroup.getName() + colourMode + ": ").create());
                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Bottom-kick: " + (serverGroup.isBottomKick() ? ChatColor.GREEN + "Yes" : ChatColor.YELLOW + "No")).create());
                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Spread Players: " + (serverGroup.isSpread() ? ChatColor.GREEN + "Yes" : ChatColor.YELLOW + "No")).create());
                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Spread Mode: " + ChatColor.YELLOW + serverGroup.getSpreadMode().toString()).create());
                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Min. Progressive: " + ChatColor.YELLOW + serverGroup.getMinimalProgressive()).create());
                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Parent Group: " + (serverGroup.getParent() != null ? ChatColor.GREEN + serverGroup.getParent().getName() : ChatColor.YELLOW + "None")).create());
                if(serverGroup.getPermission() != null) {
                    commandSender.sendMessage(new ComponentBuilder(colourMode + "  Permission: " + ChatColor.YELLOW + serverGroup.getPermission()).create());
                }

                int onlinePlayers = serverGroup.getServers().stream()
                        .filter(server -> server.isOnline())
                        .mapToInt(server -> server.getOnlinePlayersCount())
                        .sum();
                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Players (aprox): " + (onlinePlayers == 0 ? ChatColor.RED.toString() + onlinePlayers : ChatColor.YELLOW.toString() + onlinePlayers)).create());


                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Servers: ").create());
                for(RedirectServerWrapper server : serverGroup.getServers()) {
                    commandSender.sendMessage(new ComponentBuilder("    " + (server.isOnline() ? ChatColor.GREEN : ChatColor.RED) + server.getServerInfo().getName()).create());
                }

                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Connected: ").create());
                for(RedirectServerWrapper server : serverGroup.getConnected()) {
                    commandSender.sendMessage(new ComponentBuilder("    " + (server.isOnline() ? ChatColor.GREEN : ChatColor.RED) + server.getServerInfo().getName()).create());
                }

                commandSender.sendMessage(new ComponentBuilder(colourMode + "  Aliases: ").create());
                for(String alias : serverGroup.getAliases()) {
                    commandSender.sendMessage(new ComponentBuilder("    " + ChatColor.YELLOW + alias).create());
                }

                commandSender.sendMessage(new ComponentBuilder(colourMode + " ").create());
            }
            return;
        }
        sendHelp(commandSender, colourMode);
    }

    private void sendHelp(CommandSender commandSender, String colourMode){
        sendHeader(commandSender, colourMode);
        commandSender.sendMessage(new ComponentBuilder("  " + ChatColor.GREEN + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion()).create());
        commandSender.sendMessage(new ComponentBuilder(colourMode + "  Use " + ChatColor.GREEN + "/rd help" + colourMode + " for a list of commands.").create());
    }

    private void sendHeader(CommandSender commandSender, String colourMode){
        commandSender.sendMessage(new ComponentBuilder(ChatColor.DARK_GRAY + "+──────┤ " + ChatColor.BLUE + ChatColor.BOLD + "Redirect" + ChatColor.GOLD + ChatColor.BOLD + " + " + ChatColor.DARK_GRAY + "├──────+").create());
    }
}
