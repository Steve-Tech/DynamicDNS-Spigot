package me.stevetech;

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
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DynamicDNS extends JavaPlugin implements Listener {

    private final String messagePrefix = ChatColor.GOLD + "[DynamicDNS] " + ChatColor.RESET;

    @Override
    public void onEnable() {
        getLogger().info("DynamicDNS " + this.getDescription().getVersion() + " has been Enabled");
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (!(getConfig().getBoolean("afraid.enabled") ||
                getConfig().getBoolean("duckdns.enabled") ||
                getConfig().getBoolean("dynu.enabled") ||
                getConfig().getBoolean("noip.enabled") ||
                getConfig().getBoolean("custom.enabled")  ||
                getConfig().getBoolean("namecheap.enabled"))) {
            getLogger().warning("No DDNS services are enabled, Disabling Plugin");
            this.getPluginLoader().disablePlugin(this);
        } else updateIPTimer();
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("DynamicDNS " + this.getDescription().getVersion() + " has been Disabled");
    }

    public boolean updateAfraid(String token, String ip) {
        try {
            URL url = new URL("https://freedns.afraid.org/dynamic/update.php?" + token + "&address=" + ip);
            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            if (data.startsWith("Updated")) {
                getLogger().info("Updated IP on Afraid.org");
                return true;
            } else if (data.endsWith("has not changed.")) {
                getLogger().info("IP had not changed on Afraid.org");
                return true;
            } else if (data.startsWith("ERROR: ")) {
                getLogger().warning("Error updating IP on Afraid.org: " + data.replace("ERROR: ", ""));
            } else {
                getLogger().warning("Error updating IP on Afraid.org");
            }

        } catch (Exception e) {
            getLogger().severe(e.getMessage());
        }
        return false;
    }

    public boolean updateDuckDNS(String domain, String token, String ip) {
        try {
            URL url = new URL("https://www.duckdns.org/update?domains=" + domain + "&token=" + token + "&ip=" + ip);
            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            if (data.equals("OK")) {
                getLogger().info("Updated IP on DuckDNS");
                return true;
            } else if (data.equals("KO")) {
                getLogger().warning("Error updating IP on DuckDNS: Check your Domain and Token are correct in the config");
            } else {
                getLogger().warning("Error updating IP on DuckDNS");
            }

        } catch (Exception e) {
            getLogger().severe(e.getMessage());
        }
        return false;
    }

    public boolean updateDynu(String hostname, String password, String ip) {
        try {
            URL url = new URL("https://api.dynu.com/nic/update?hostname=" + hostname + "&myip=" + ip + "&password=" + password);
            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            if (data.startsWith("good")) {
                getLogger().info("Updated IP on Dynu");
                return true;
            } else if (data.equals("nochg")) {
                getLogger().info("IP had not changed on Dynu");
                return true;
            } else if (data.equals("badauth")) {
                getLogger().warning("Error updating IP on Dynu: Check your Hostname and Password are correct in the config");
            } else {
                getLogger().warning("Error updating IP on Dynu");
            }

        } catch (Exception e) {
            getLogger().severe(e.getMessage());
        }
        return false;
    }

    public boolean updateNoIP(String hostname, String username, String password, String ip) {
        try {
            URL url = new URL("https://dynupdate.no-ip.com/nic/update?hostname=" + hostname + "&myip=" + ip);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((username+":"+password).getBytes(StandardCharsets.UTF_8))));
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            if (data.startsWith("good")) {
                getLogger().info("Updated IP on No-IP");
                return true;
            } else if (data.startsWith("nochg")) {
                getLogger().info("IP had not changed on No-IP");
                return true;
            } else {
                getLogger().warning("Error updating IP on No-IP");
            }

        } catch (Exception e) {
            getLogger().severe(e.getMessage());
        }
        return false;
    }

    public boolean updateNameCheap(String subdomain, String domain, String token, String ip) {
        try {
            URL url = new URL("https://dynamicdns.park-your-domain.com/update?host=" + subdomain + "&domain=" + domain + "&password=" + token + "&ip=" + ip);

            getLogger().info(url.toString());

            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            if(!data.isEmpty()) {

                getLogger().info("Updated IP on Namecheap");
                return true;
            } else {
                getLogger().warning("Error updating IP on Namecheap");
                return false;
            }

        } catch (Exception e) {
            getLogger().severe(e.getMessage());
        }
        return false;
    }
    public boolean updateCustom(String updateURL, String ip) {
        try {
            URL url = new URL(updateURL.replaceAll("(?i)%ip%", ip));
            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();

            getLogger().info("Attempted to update IP on Custom DDNS: " + data);
            return true;

        } catch (Exception e) {
            getLogger().severe(e.getMessage());
        }
        return false;
    }

    public boolean updateIP(String ip) {
        boolean success = true;
        if (getConfig().getBoolean("afraid.enabled")) {
            if (!updateAfraid(getConfig().getString("afraid.token"), ip)) {
                success = false;
            }
        }
        if (getConfig().getBoolean("duckdns.enabled")) {
            if (!updateDuckDNS(getConfig().getString("duckdns.domain"), getConfig().getString("duckdns.token"), ip)) {
                success = false;
            }
        }
        if (getConfig().getBoolean("dynu.enabled")) {
            if (!updateDynu(getConfig().getString("dynu.hostname"), getConfig().getString("dynu.password"), ip)) {
                success = false;
            }
        }
        if (getConfig().getBoolean("noip.enabled")) {
            if (!updateNoIP(getConfig().getString("noip.hostname"), getConfig().getString("noip.username"), getConfig().getString("noip.password"), ip)) {
                success = false;
            }
        }
        if (getConfig().getBoolean("custom.enabled")) {
            if (!updateCustom(getConfig().getString("custom.url"), ip)) {
                success = false;
            }
        }
        if (getConfig().getBoolean("namecheap.enabled")) {
            if (!updateNameCheap(getConfig().getString("namecheap.subdomain"), getConfig().getString("namecheap.domain"), getConfig().getString("namecheap.token"), ip)) {
                success = false;
            }
        }
        return success;
    }

    private void updateIPTimer() {
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> updateIP(""), (0), (getConfig().getInt("period") * 20L));
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if ((cmd.getName().equalsIgnoreCase("updateip")) && (sender.hasPermission("DynamicDNS.update"))) {
            if ((args.length == 1) && (sender.hasPermission("DynamicDNS.update.ip"))) {
                Bukkit.getServer().getScheduler().runTaskAsynchronously(this, () -> {
                    if (updateIP(args[0]) && sender instanceof Player) {
                        sender.sendMessage(messagePrefix + "Updated IP.");
                    } else if (sender instanceof Player) {
                        sender.sendMessage(messagePrefix + "Error updating IP, check the log for details.");
                    }
                });
            } else {
                Bukkit.getServer().getScheduler().runTaskAsynchronously(this, () -> {
                    if (updateIP("") && sender instanceof Player) {
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
