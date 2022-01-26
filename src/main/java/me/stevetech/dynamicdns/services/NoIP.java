package me.stevetech.dynamicdns.services;

import me.stevetech.dynamicdns.DDNSService;
import me.stevetech.dynamicdns.DynamicDNS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Steve-Tech
 */

public class NoIP extends DDNSService {
    private final DynamicDNS plugin;

    public NoIP(DynamicDNS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean enabled() {
        return plugin.getConfig().getBoolean("noip.enabled");
    }

    @Override
    public boolean update(String ip) {
        String hostname = plugin.getConfig().getString("noip.hostname");
        String username = plugin.getConfig().getString("noip.username");
        String password = plugin.getConfig().getString("noip.password");
        try {
            URL url = new URL("https://dynupdate.no-ip.com/nic/update?hostname=" + hostname + "&myip=" + ip);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes(StandardCharsets.UTF_8))));
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            if (data.startsWith("good")) {
                plugin.getLogger().info("Updated IP on No-IP");
                return true;
            } else if (data.startsWith("nochg")) {
                plugin.getLogger().info("IP had not changed on No-IP");
                return true;
            } else {
                plugin.getLogger().warning("Error updating IP on No-IP");
            }

        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }
}
