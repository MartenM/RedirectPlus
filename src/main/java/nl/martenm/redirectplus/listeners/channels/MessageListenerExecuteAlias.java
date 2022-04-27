package nl.martenm.redirectplus.listeners.channels;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import nl.martenm.redirectplus.RedirectPlus;

/**
 * This class allows other plugins to use the BungeeCord plugin messaging system to send people to a certain server group.
 * Arguments:
 *  - playername
 *  - servergroup
 */
public class MessageListenerExecuteAlias implements Listener {

    public static final String COMMAND = "execute-alias";

    private RedirectPlus plugin;
    public MessageListenerExecuteAlias(RedirectPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void messageReceived(PluginMessageEvent event) {
        if(!event.getTag().equalsIgnoreCase(RedirectPlus.CHANNEL_NAME)) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subChannel = in.readUTF();

        if(!subChannel.equalsIgnoreCase(COMMAND)) return;

        // Only valid when talking to the proxy.  (server -> proxy)
        if(!(event.getReceiver() instanceof ProxiedPlayer)) return;

        String playerName = in.readUTF(); // data1
        String alias = in.readUTF(); // data2

        ProxiedPlayer proxiedPlayer = plugin.getProxy().getPlayer(playerName);

        if(proxiedPlayer == null) {
            plugin.getLogger().warning(String.format("Cannot execute alias for %s. Player not found with that name!", playerName));
            return;
        }

        plugin.getAliasManager().handleAliasExecution(alias, proxiedPlayer);
    }
}
