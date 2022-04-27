package nl.martenm.redirectplus.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import nl.martenm.redirectplus.RedirectPlus;
import nl.martenm.redirectplus.api.events.ProxiedPlayerGroupAliasExecuted;
import nl.martenm.redirectplus.objects.RedirectServerWrapper;
import nl.martenm.redirectplus.objects.ServerGroup;

public class AliasCommand extends Command {

    private RedirectPlus plugin;
    private ServerGroup serverGroup;

    public AliasCommand(RedirectPlus plugin, ServerGroup serverGroup, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
        this.serverGroup = serverGroup;
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(TextComponent.fromLegacyText("&cOnly the sender can execute a redirect command."));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if(serverGroup.isRestricted()) {
            if(!player.hasPermission(serverGroup.getPermission())) {
                for (String message : plugin.getConfig().getStringList("messages.alias-no-permission")) {
                    message = ChatColor.translateAlternateColorCodes('&', message);
                    player.sendMessage(TextComponent.fromLegacyText(message));
                }
                return;
            }
        }

        ServerInfo currentServer = player.getServer().getInfo();
        ServerGroup currentServerGroup = null;
        RedirectServerWrapper currentServerWrapper = plugin.getServer(currentServer.getName());

        // Check if the player is in the same server group as the destination
        // And check if the alias execution is allowed by the server.
        if(currentServerWrapper != null) {

            if (!currentServerWrapper.isAllowAliases()) {
                for (String message : plugin.getConfig().getStringList("messages.alias-not-allowed-server")) {
                    message = ChatColor.translateAlternateColorCodes('&', message);
                    player.sendMessage(TextComponent.fromLegacyText(message));
                }
                return;
            }

            currentServerGroup = currentServerWrapper.getServerGroup();

            // Possibly the same server group and only one server.
            // In that case redirection is not possible.

            // Check if the is the same and the amount of servers is 1
            if(serverGroup.equals(currentServerGroup) && serverGroup.getAvailableServersSize() <= 1) {

                // Check if the server group is the same OR if if the server group has 0 available servers.
                if(serverGroup.getAvailableServersSize() == 0 || serverGroup.getServers().get(0).equals(currentServerWrapper)) {
                    for (String message : plugin.getConfig().getStringList("messages.unable-redirect-alias-same-category")) {
                        message = ChatColor.translateAlternateColorCodes('&', message);
                        player.sendMessage(TextComponent.fromLegacyText(message));
                    }
                    return;
                }
            }
        }

        RedirectServerWrapper server = serverGroup.getRedirectServer(player, player.getServer().getInfo().getName(), false, serverGroup.getSpreadMode());

        if(server == null) {
            for (String message : plugin.getConfig().getStringList("messages.unable-redirect-alias")) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                player.sendMessage(TextComponent.fromLegacyText(message));
            }
            return;
        }

        // Redirect API
        ProxiedPlayerGroupAliasExecuted apiEvent = new ProxiedPlayerGroupAliasExecuted(player, getName(), currentServerGroup, server);
        plugin.getProxy().getPluginManager().callEvent(apiEvent);

        if(apiEvent.isCancelled()) {
            return;
        }
        //

        player.connect(server.getServerInfo());
        server.addProxiedPlayer();
        if(currentServerWrapper != null) {
            currentServerWrapper.removeProxiedPlayer();
        }
    }
}
