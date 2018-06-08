package nl.martenm.redirectplus.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
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

        if(strings.length == 0){
            sendHelp(commandSender);
            return;
        }

        if(strings[0].equalsIgnoreCase("help")){
            sendHeader(commandSender);
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "  " + "/rd servers").create());
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "  " + "/rd groups").create());
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "  " + "/rd reload").create());
            return;
        }

        else if(strings[0].equalsIgnoreCase("reload")){
            plugin.reload();
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "" + "Successfully reloaded the config file.").create());
            return;
        }

        else if(strings[0].equalsIgnoreCase("servers")){
            sendHeader(commandSender);
            for(RedirectServerWrapper server : plugin.getRedirectServers())
            {
                commandSender.sendMessage(new ComponentBuilder(ChatColor.WHITE + ChatColor.BOLD.toString() + server.getServerInfo().getName() + ChatColor.GRAY + ": ").create());
                commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + "  Status: " + (server.isOnline() ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline")).create());
                commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + "  Group: " + ChatColor.YELLOW + server.getServerGroup().getName()).create());
                commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + "  Receives Redirects: " + (server.isRedirectable() ? ChatColor.GREEN + "Yes" : ChatColor.YELLOW + "No")).create());
                commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + "  Moth: " + ChatColor.GRAY + server.getServerInfo().getMotd()).create());
                commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + " ").create());
            }
            return;
        }
        else if(strings[0].equalsIgnoreCase("groups")){
            sendHeader(commandSender);
            for(ServerGroup serverGroup : plugin.getServerGroups())
            {
                commandSender.sendMessage(new ComponentBuilder(ChatColor.WHITE + ChatColor.BOLD.toString() + serverGroup.getName() + ChatColor.GRAY + ": ").create());
                commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + "  Bottom-kick: " + (serverGroup.isBottomKick() ? ChatColor.GREEN + "Yes" : ChatColor.YELLOW + "No")).create());
                commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + "  Spread Players: " + (serverGroup.isSpread() ? ChatColor.GREEN + "Yes" : ChatColor.YELLOW + "No")).create());
                commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + "  Parent Group: " + (serverGroup.getParent() != null ? ChatColor.GREEN + serverGroup.getParent().getName() : ChatColor.YELLOW + "None")).create());

                commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + "  Servers: ").create());
                for(RedirectServerWrapper server : serverGroup.getServers()) {
                    commandSender.sendMessage(new ComponentBuilder("    " + (server.isOnline() ? ChatColor.GREEN : ChatColor.RED) + server.getServerInfo().getName()).create());
                }
                commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + "  Connected: ").create());
                for(RedirectServerWrapper server : serverGroup.getConnected()) {
                    commandSender.sendMessage(new ComponentBuilder("    " + (server.isOnline() ? ChatColor.GREEN : ChatColor.RED) + server.getServerInfo().getName()).create());
                }
                commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + " ").create());
            }
            return;
        }
        sendHelp(commandSender);
    }

    private void sendHelp(CommandSender commandSender){
        sendHeader(commandSender);
        commandSender.sendMessage(new ComponentBuilder("  " + ChatColor.GREEN + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion()).create());
        commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + "  Use " + ChatColor.GREEN + "/rd help" + ChatColor.GRAY + " for a list of commands.").create());
    }

    private void sendHeader(CommandSender commandSender){
        commandSender.sendMessage(new ComponentBuilder(ChatColor.DARK_GRAY + "+──────┤ " + ChatColor.BLUE + ChatColor.BOLD + "Redirect" + ChatColor.GOLD + ChatColor.BOLD + " + " + ChatColor.DARK_GRAY + "├──────+").create());
    }
}
