package nl.martenm.redirect.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import nl.martenm.redirect.RedirectPlus;
import nl.martenm.redirect.objects.RedirectServerWrapper;

/**
 * @author MartenM
 * @since 5-1-2018.
 */
public class RedirectCommand extends Command {

    private final RedirectPlus plugin;

    public RedirectCommand(RedirectPlus plugin) {
        super("redirect", "redirectplus.admin", "rd");
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
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "  " + "/rd reload").create());
            return;
        }

        else if(strings[0].equalsIgnoreCase("reload")){
            plugin.reload();
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "  " + "Successfully reloaded the config file.").create());
            return;
        }

        else if(strings[0].equalsIgnoreCase("servers")){
            sendHeader(commandSender);
            commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "Online servers:").create());
            for(RedirectServerWrapper info : plugin.getOnlineServer()){
                commandSender.sendMessage(new ComponentBuilder("  " + ChatColor.GRAY + info.getServerInfo().getName()).create());
            }
            commandSender.sendMessage(new ComponentBuilder("").create());
            commandSender.sendMessage(new ComponentBuilder(ChatColor.RED + "Offline servers:").create());
            for(RedirectServerWrapper info: plugin.getOfflineServer()){
                commandSender.sendMessage(new ComponentBuilder("  " + ChatColor.GRAY + info.getServerInfo().getName()).create());
            }
            return;
        }
        sendHelp(commandSender);
    }

    private void sendHelp(CommandSender commandSender){
        sendHeader(commandSender);
        commandSender.sendMessage(new ComponentBuilder(ChatColor.GREEN + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion()).create());
        commandSender.sendMessage(new ComponentBuilder(ChatColor.GRAY + "Use " + ChatColor.GREEN + "/rd help" + ChatColor.GRAY + " for a list of commands.").create());
    }

    private void sendHeader(CommandSender commandSender){
        commandSender.sendMessage(new ComponentBuilder(ChatColor.DARK_GRAY + "+──────┤ " + ChatColor.BLUE + ChatColor.BOLD + "Redirect" + ChatColor.GOLD + ChatColor.BOLD + " + " + ChatColor.DARK_GRAY + "├──────+").create());
    }
}
