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

public class Custom extends DDNSService {
    private final DynamicDNS plugin;

    public Custom(DynamicDNS plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "Custom";
    }

    @Override
    public boolean enabled() {
        return plugin.getConfig().getBoolean("custom.enabled");
    }

    @Override
    public boolean update(String ip) {
        String updateURL = plugin.getConfig().getString("custom.url");
        try {
            URL url = new URL(updateURL.replaceAll("(?i)%ip%", ip));
            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();

            plugin.getLogger().info("Attempted to update IP on Custom DDNS: " + data);
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }
}
