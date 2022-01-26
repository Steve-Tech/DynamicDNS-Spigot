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

public class Dynu extends DDNSService {
    private final DynamicDNS plugin;

    public Dynu(DynamicDNS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean enabled() {
        return plugin.getConfig().getBoolean("dynu.enabled");
    }

    @Override
    public boolean update(String ip) {
        String hostname = plugin.getConfig().getString("dynu.hostname");
        String password = plugin.getConfig().getString("dynu.password");
        try {
            URL url = new URL("https://api.dynu.com/nic/update?hostname=" + hostname + "&myip=" + ip + "&password=" + password);
            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            if (data.startsWith("good")) {
                plugin.getLogger().info("Updated IP on Dynu");
                return true;
            } else if (data.equals("nochg")) {
                plugin.getLogger().info("IP had not changed on Dynu");
                return true;
            } else if (data.equals("badauth")) {
                plugin.getLogger().warning("Error updating IP on Dynu: Check your Hostname and Password are correct in the config");
            } else {
                plugin.getLogger().warning("Error updating IP on Dynu");
            }

        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }
}
