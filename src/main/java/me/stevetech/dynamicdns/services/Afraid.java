package me.stevetech.dynamicdns.services;

import me.stevetech.dynamicdns.DynamicDNS;
import me.stevetech.dynamicdns.DDNSService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Steve-Tech
 */

public class Afraid extends DDNSService {
    private final DynamicDNS plugin;

    public Afraid(DynamicDNS plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "Afraid";
    }

    @Override
    public boolean enabled() {
        return plugin.getConfig().getBoolean("afraid.enabled");
    }

    @Override
    public boolean update(String ip) {
        String token = plugin.getConfig().getString("afraid.token");

        try {
            URL url = new URL("https://freedns.afraid.org/dynamic/update.php?" + token + "&address=" + ip);
            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            if (data.startsWith("Updated")) {
                plugin.getLogger().info("Updated IP on Afraid.org");
                return true;
            } else if (data.endsWith("has not changed.")) {
                plugin.getLogger().info("IP had not changed on Afraid.org");
                return true;
            } else if (data.startsWith("ERROR: ")) {
                plugin.getLogger().warning("Error updating IP on Afraid.org: " + data.replace("ERROR: ", ""));
            } else {
                plugin.getLogger().warning("Error updating IP on Afraid.org");
            }

        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }
}
