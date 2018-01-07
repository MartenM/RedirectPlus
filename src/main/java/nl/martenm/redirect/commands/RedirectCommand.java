package nl.martenm.redirect.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;
import nl.martenm.redirect.RedirectPlus;
import nl.martenm.redirect.objects.PriorityWrapper;

/**
 * @author MartenM
 * @since 5-1-2018.
 */
public class RedirectCommand extends Command {

    private RedirectPlus plugin;

    public RedirectCommand(RedirectPlus plugin) {
        super("redirect", "redirectplus", "rd");
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
            commandSender.sendMessage(new ComponentBuilder(ChatColor.DARK_GRAY + "+──────┤ " + ChatColor.GREEN + ChatColor.BOLD + "Redirect" + ChatColor.GOLD + " + " + ChatColor.DARK_GRAY + "├──────+").create());
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "  " + "/rd servers").create());
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "  " + "/rd reload").create());
            return;
        }

        else if(strings[0].equalsIgnoreCase("servers")){
            commandSender.sendMessage(new ComponentBuilder(ChatColor.DARK_GRAY + "+──────┤ " + ChatColor.GREEN + ChatColor.BOLD + "Redirect" + ChatColor.GOLD + " + " + ChatColor.DARK_GRAY + "├──────+").create());
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "Online servers:").create());
            for(PriorityWrapper info : plugin.getOnlineServer()){
                commandSender.sendMessage(new ComponentBuilder("  " + ChatColor.GRAY + info.getServerInfo().getName()).create());
            }
            commandSender.sendMessage(new ComponentBuilder("").create());
            commandSender.sendMessage(new ComponentBuilder(ChatColor.RED + "Offline servers:").create());
            for(PriorityWrapper info: plugin.getOfflineServer()){
                commandSender.sendMessage(new ComponentBuilder("  " + ChatColor.GRAY + info.getServerInfo().getName()).create());
            }
            return;
        }
        sendHelp(commandSender);
    }

    private void sendHelp(CommandSender commandSender){
        commandSender.sendMessage(new ComponentBuilder(ChatColor.DARK_GRAY + "+──────┤ " + ChatColor.GREEN + ChatColor.BOLD + "Redirect" + ChatColor.GOLD + " + " + ChatColor.DARK_GRAY + "├──────+").create());
        commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion()).create());
        commandSender.sendMessage(new ComponentBuilder(ChatColor.DARK_GRAY + "Use " + ChatColor.GREEN + "/rd help" + ChatColor.DARK_GRAY + " for a list of commands.").create());
    }
}
