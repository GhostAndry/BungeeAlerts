package me.ghostdevelopment.bungeealerts.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeMessage implements PluginMessageListener {
  public void onPluginMessageReceived(String channel, Player player, byte[] message) {
    if (channel.equalsIgnoreCase("BungeeCord")) {
      ByteArrayDataInput in = ByteStreams.newDataInput(message);
      String subchannel = in.readUTF();
      if (subchannel.equals("ACALERTS")) {
        String flag = in.readUTF();
        for (Player staffer : BungeeAlerts.getStaffer())
          staffer.sendMessage(flag); 
        Bukkit.getLogger().info(flag);
      } 
    } 
  }
}
