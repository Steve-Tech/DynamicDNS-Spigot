package me.stevetech.dynamicdns;

import me.stevetech.dynamicdns.services.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DynamicDNS: A dynamic DNS plugin for Spigot
 * @author Steve-Tech
 */

public class DynamicDNS extends JavaPlugin implements Listener {

    private final String messagePrefix = ChatColor.GOLD + "[DynamicDNS] " + ChatColor.RESET;

    private BukkitTask task;

    public ArrayList<DDNSService> services = new ArrayList<>();
    // In theory, other plugins can add their own service by adding to this ArrayList

    public void loadServices() {
        // idk how reflection works, add your new services here:
        services.add(new Afraid(this));
        services.add(new DuckDNS(this));
        services.add(new Dynu(this));
        services.add(new NoIP(this));
        services.add(new NameCheap(this));
        services.add(new Cloudflare(this));

        services.add(new Custom(this));
    }

    public void setupServices() {
        services.forEach(service -> {
            if (service.enabled()) {
                service.setup();
            }
        });
    }

    @Override
    public void onEnable() {
        getLogger().info("DynamicDNS " + this.getDescription().getVersion() + " has been Enabled");
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadServices();
        setupServices();
        updateIPTimer();
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("DynamicDNS " + this.getDescription().getVersion() + " has been Disabled");
    }

    public boolean updateIP(String ip) {
        AtomicBoolean success = new AtomicBoolean(true);
        services.forEach(service -> {
            if (service.enabled() && !service.update(ip)) {
                success.set(false);
            }
        });
        return success.get();
    }

    private void updateIPTimer() {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> updateIP(""), (0), (getConfig().getInt("period") * 20L));
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if ((cmd.getName().equalsIgnoreCase("dynamicdns")) && (sender.hasPermission("dynamicdns"))) {
            if (args.length > 0) {
                switch (args[0]) {
                    case "update":
                        if (sender.hasPermission("dynamicdns.update")) {
                            if ((args.length > 2) && (sender.hasPermission("dynamicdns.update.ip"))) {
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
                    case "list":
                        if (sender.hasPermission("dynamicdns.list")) {
                            StringBuilder enabled = new StringBuilder();
                            services.forEach(service -> {
                                if (service.enabled()) {
                                    enabled.append(service.name());
                                    enabled.append(" ");
                                }
                            });
                            sender.sendMessage(messagePrefix + "Loaded Services: " + enabled);
                        }
                        return true;
                    case "reload":
                        if (sender.hasPermission("dynamicdns.reload")) {
                            reloadConfig();
                            updateIPTimer();
                            sender.sendMessage(messagePrefix + "Reloaded Config");
                        }
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if ((cmd.getName().equalsIgnoreCase("dynamicdns")) && (sender.hasPermission("dynamicdns"))) {
            if (args.length == 1) {
                return new ArrayList<>() {{
                    if (sender.hasPermission("dynamicdns.update"))
                        add("update");
                    if (sender.hasPermission("dynamicdns.list"))
                        add("list");
                    if (sender.hasPermission("dynamicdns.reload"))
                        add("reload");
                }};
            }
            return new ArrayList<>();
        }
        return null;
    }
}
