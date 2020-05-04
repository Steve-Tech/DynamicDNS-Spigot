package me.steve8playz;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class DuckDNS extends JavaPlugin implements Listener {

    private final String messagePrefix = ChatColor.GOLD + "[DuckDNS] " + ChatColor.RESET;

    @Override
    public void onEnable() {
        getLogger().info("DuckDNS v1.0 has been Enabled");
        getConfig().options().copyDefaults(true);
        saveConfig();
        updateIPTimer();
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("DuckDNS v1.0 has been Disabled");
    }

    public boolean updateIP(final String domain, String token, String ip) {
        try {
            URL url = new URL("https://www.duckdns.org/update?domains=" + domain + "&token=" + token + "&ip=" + ip);
            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            if (data.equals("OK")) {
                getLogger().info("Updated IP.");
                return true;
            } else if (data.equals("KO")) {
                getLogger().warning("Configuration Error: Check your Token and Domain are correct in the config.");
            } else {
                getLogger().warning("Connection Error: Error updating IP.");
            }

        } catch (Exception e) {
            getLogger().severe(e.getMessage() + e.toString() + e.getCause());
        }
        return false;
    }

    private void updateIPTimer() {
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> updateIP(getConfig().getString("domain"), getConfig().getString("token"), ""), (0), (getConfig().getInt("period") * 20L));
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if ((cmd.getName().equalsIgnoreCase("updateip")) && (sender.hasPermission("duckdns.update"))) {
            if ((args.length == 1) && (sender.hasPermission("duckdns.update.ip"))) {
                Bukkit.getServer().getScheduler().runTaskAsynchronously(this, () -> {
                    if (updateIP(getConfig().getString("domain"), getConfig().getString("token"), args[0]) && sender instanceof Player) {
                        sender.sendMessage(messagePrefix + "Updated IP.");
                    } else if (sender instanceof Player) {
                        sender.sendMessage(messagePrefix + "Error updating IP, check the log for details.");
                    }
                });
            } else {
                Bukkit.getServer().getScheduler().runTaskAsynchronously(this, () -> {
                    if (updateIP(getConfig().getString("domain"), getConfig().getString("token"), "") && sender instanceof Player) {
                        sender.sendMessage(messagePrefix + "Updated IP.");
                    } else if (sender instanceof Player) {
                        sender.sendMessage(messagePrefix + "Error updating IP, check the log for details.");
                    }
                });
            }
        }
        return true;
    }
}