package me.stevetech.dynamicdns.services;

import me.stevetech.dynamicdns.DDNSService;
import me.stevetech.dynamicdns.DynamicDNS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

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

        //Ternary operation to check if more than 1 subdomain is provided
        String[] subdomains = (plugin.getConfig().getString("namecheap.subdomain").split(",").length > 1) ?
                plugin.getConfig().getString("namecheap.subdomain").split(",")
                : new String[]{plugin.getConfig().getString("namecheap.subdomain")};
        String domain = plugin.getConfig().getString("namecheap.domain");
        String token = plugin.getConfig().getString("namecheap.token");

        for (String subdomain : subdomains) {
            try {
                URL url = new URL("https://dynamicdns.park-your-domain.com/update?host=" + subdomain + "&domain=" + domain + "&password=" + token + "&ip=" + ip);
                URLConnection conn = url.openConnection();

                //Namecheap DDNS returns an XML response, so some silly String parsing shenanigans take place
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String ddnsResponse = bufferedReader.lines().collect(Collectors.joining("\n"));

                if (ddnsResponse.contains("<ErrCount>1</ErrCount>")) {
                    if (ddnsResponse.contains("<Description>Domain name not found</Description>")) {
                        plugin.getLogger().warning("Error updating IP on Namecheap, domain name \"" + domain + "\" not found");
                        return false;
                    } else if (ddnsResponse.contains("<Description>No Records updated. A record not Found;</Description>")) {
                        plugin.getLogger().warning("Error updating IP on Namecheap, no A record exists for subdomain \"" + subdomain + "\"");
                        return false;
                    } else if (ddnsResponse.contains("<Description>Passwords do not match</Description>")) {
                        plugin.getLogger().warning("Error updating IP on Namecheap, password is incorrect");
                        return false;
                    }
                } else {
                    plugin.getLogger().info("Updated IP for " + subdomain + "." + domain + " on namecheap");
                    return true;
                }

            } catch (Exception e) {
                plugin.getLogger().severe(e.getMessage());
            }
        }
        return false;
    }
}
