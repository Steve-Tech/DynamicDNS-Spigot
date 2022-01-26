package me.stevetech.dynamicdns.services;

import me.stevetech.dynamicdns.DDNSService;
import me.stevetech.dynamicdns.DynamicDNS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Steve-Tech
 */

public class DuckDNS extends DDNSService {
    private final DynamicDNS plugin;

    public DuckDNS(DynamicDNS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean enabled() {
        return plugin.getConfig().getBoolean("duckdns.enabled");
    }

    @Override
    public boolean update(String ip) {
        String domain = plugin.getConfig().getString("duckdns.domain");
        String token = plugin.getConfig().getString("duckdns.token");

        try {
            URL url = new URL("https://www.duckdns.org/update?domains=" + domain + "&token=" + token + "&ip=" + ip);
            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            if (data.equals("OK")) {
                plugin.getLogger().info("Updated IP on DuckDNS");
                return true;
            } else if (data.equals("KO")) {
                plugin.getLogger().warning("Error updating IP on DuckDNS: Check your Domain and Token are correct in the config");
            } else {
                plugin.getLogger().warning("Error updating IP on DuckDNS");
            }

        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }
}
