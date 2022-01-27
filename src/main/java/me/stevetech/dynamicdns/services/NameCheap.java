package me.stevetech.dynamicdns.services;

import me.stevetech.dynamicdns.DDNSService;
import me.stevetech.dynamicdns.DynamicDNS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author LethalBoar70923
 */

public class NameCheap extends DDNSService {
    private final DynamicDNS plugin;

    public NameCheap(DynamicDNS plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "NameCheap";
    }

    @Override
    public boolean enabled() {
        return plugin.getConfig().getBoolean("namecheap.enabled");
    }

    @Override
    public boolean update(String ip) {
        String subdomain = plugin.getConfig().getString("namecheap.subdomain");
        String domain = plugin.getConfig().getString("namecheap.domain");
        String token = plugin.getConfig().getString("namecheap.token");
        try {
            URL url = new URL("https://dynamicdns.park-your-domain.com/update?host=" + subdomain + "&domain=" + domain + "&password=" + token + "&ip=" + ip);
            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();

            if (!data.isEmpty()) {
                plugin.getLogger().info("Updated IP on Namecheap");
                return true;
            } else {
                plugin.getLogger().warning("Error updating IP on Namecheap");
                return false;
            }

        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }
}
