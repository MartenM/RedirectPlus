package nl.martenm.redirectplus.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
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
        commandSender.sendMessage(TextComponent.fromLegacyText(""));

        String colourMode = ChatColor.GRAY.toString();
        if(!(commandSender instanceof ProxiedPlayer)) {
           colourMode = ChatColor.WHITE.toString();
        }

        if(plugin.isDisabled()) {
            sendHeader(commandSender, colourMode);
            commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "  " + "The plugin has been disabled."));
            commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "  " + "Please check the console for more information.."));
            return;
        }

        if(strings.length == 0){
            sendHelp(commandSender, colourMode);
            return;
        }

        if(strings[0].equalsIgnoreCase("help")){
            sendHeader(commandSender, colourMode);
            commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "  " + "/rd servers"));
            commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "  " + "/rd groups"));
            commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "  " + "/rd refresh"));
            commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "  " + "/rd reload"));
            return;
        }

        else if(strings[0].equalsIgnoreCase("reload")){
            plugin.reload();
            commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "" + "Successfully reloaded the config file."));
            return;
        }

        else if(strings[0].equalsIgnoreCase("refresh")){
            plugin.updateServers();
            commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "" + "Refreshing server info right now. Pleas note that this may take some time (1-10 seconds)."));
            return;
        }

        else if(strings[0].equalsIgnoreCase("servers")){
            sendHeader(commandSender, colourMode);
            for(RedirectServerWrapper server : plugin.getRedirectServers())
            {
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.WHITE + ChatColor.BOLD.toString() + server.getServerInfo().getName() + colourMode + ": "));
                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Status: " + (server.isOnline() ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline")));
                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Group: " + ChatColor.YELLOW + server.getServerGroup().getName()));
                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Receives Redirects: " + (server.isRedirectable() ? ChatColor.GREEN + "Yes" : ChatColor.YELLOW + "No")));
                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Players (aprox): " + (server.getOnlinePlayersCount() == 0 ? ChatColor.RED.toString() + server.getOnlinePlayersCount() : ChatColor.YELLOW.toString() + server.getOnlinePlayersCount())));

                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Moth: " + colourMode + server.getServerInfo().getMotd()));
                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + " "));
            }
            return;
        }
        else if(strings[0].equalsIgnoreCase("groups")){
            sendHeader(commandSender, colourMode);
            for(ServerGroup serverGroup : plugin.getServerGroups())
            {
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.WHITE + ChatColor.BOLD.toString() + serverGroup.getName() + colourMode + ": "));
                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Bottom-kick: " + (serverGroup.isBottomKick() ? ChatColor.GREEN + "Yes" : ChatColor.YELLOW + "No")));
                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Spread Players: " + (serverGroup.isSpread() ? ChatColor.GREEN + "Yes" : ChatColor.YELLOW + "No")));
                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Spread Mode: " + ChatColor.YELLOW + serverGroup.getSpreadMode().toString()));
                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Min. Progressive: " + ChatColor.YELLOW + serverGroup.getMinimalProgressive()));
                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Parent Group: " + (serverGroup.getParent() != null ? ChatColor.GREEN + serverGroup.getParent().getName() : ChatColor.YELLOW + "None")));
                if(serverGroup.isRestricted()) {
                    commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Permission: " + ChatColor.YELLOW + serverGroup.getPermission()));
                }

                int onlinePlayers = serverGroup.getServers().stream()
                        .filter(server -> server.isOnline())
                        .mapToInt(server -> server.getOnlinePlayersCount())
                        .sum();
                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Players (aprox): " + (onlinePlayers == 0 ? ChatColor.RED.toString() + onlinePlayers : ChatColor.YELLOW.toString() + onlinePlayers)));


                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Servers: "));
                for(RedirectServerWrapper server : serverGroup.getServers()) {
                    commandSender.sendMessage(TextComponent.fromLegacyText("    " + (server.isOnline() ? ChatColor.GREEN : ChatColor.RED) + server.getServerInfo().getName()));
                }

                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Connected: "));
                for(RedirectServerWrapper server : serverGroup.getConnected()) {
                    commandSender.sendMessage(TextComponent.fromLegacyText("    " + (server.isOnline() ? ChatColor.GREEN : ChatColor.RED) + server.getServerInfo().getName()));
                }

                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Aliases: "));
                for(String alias : serverGroup.getAliases()) {
                    commandSender.sendMessage(TextComponent.fromLegacyText("    " + ChatColor.YELLOW + alias));
                }

                commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + " "));
            }
            return;
        }
        sendHelp(commandSender, colourMode);
    }

    private void sendHelp(CommandSender commandSender, String colourMode){
        sendHeader(commandSender, colourMode);
        commandSender.sendMessage(TextComponent.fromLegacyText("  " + ChatColor.GREEN + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion()));
        commandSender.sendMessage(TextComponent.fromLegacyText(colourMode + "  Use " + ChatColor.GREEN + "/rd help" + colourMode + " for a list of commands."));
    }

    private void sendHeader(CommandSender commandSender, String colourMode){
        commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GRAY + "+──────┤ " + ChatColor.BLUE + ChatColor.BOLD + "Redirect" + ChatColor.GOLD + ChatColor.BOLD + " + " + ChatColor.DARK_GRAY + "├──────+"));
    }
}
