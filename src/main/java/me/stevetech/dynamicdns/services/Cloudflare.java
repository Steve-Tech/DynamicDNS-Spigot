package me.stevetech.dynamicdns.services;

import com.google.gson.Gson;
import me.stevetech.dynamicdns.DDNSService;
import me.stevetech.dynamicdns.DynamicDNS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Steve-Tech
 */

public class Cloudflare extends DDNSService {
    private final DynamicDNS plugin;

    Gson gson = new Gson();

    public Cloudflare(DynamicDNS plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "Cloudflare";
    }

    @Override
    public boolean enabled() {
        return plugin.getConfig().getBoolean("cloudflare.enabled");
    }

    @Override
    public void setup() {
        if (enabled() && plugin.getConfig().getString("cloudflare.record_id") == null) {
            String zone_id = plugin.getConfig().getString("cloudflare.zone_id");
            String name = plugin.getConfig().getString("cloudflare.name");
            String token = plugin.getConfig().getString("cloudflare.token");
            try {
                URL url = new URL("https://api.cloudflare.com/client/v4/zones/" + zone_id + "/dns_records?name=" + name);
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.connect();

                String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
                System.out.println(data);

                Map<?, ?> json = gson.fromJson(data, Map.class);

                if (((Boolean) json.get("success")) && ((ArrayList<?>) json.get("result")).size() > 0) {
                    plugin.getConfig().set("cloudflare.record_id", ((Map<?, ?>) ((ArrayList<?>) json.get("result")).get(0)).get("id"));
                    plugin.saveConfig();
                } else {
                    plugin.getLogger().severe("An error occured setting up Cloudflare");
                    plugin.getLogger().info(data);
                }

            } catch (Exception e) {
                plugin.getLogger().severe(e.getMessage());
            }
        }
    }

    /**
     * Formats the IP from cloudflare's cdn-cgi/trace
     *
     * @param ip_url the url to get the ip from, e.g. https://1.1.1.1/cdn-cgi/trace
     * @return the ip as a string
     * @see #getIP() for getting IPv4 from 1.1.1.1
     * @see #getIPv6() for getting IPv6 from 2606:4700:4700::1111
     */
    public String getIP(String ip_url) throws IOException {
        URL url = new URL(ip_url);
        URLConnection conn = url.openConnection();
        conn.connect();
        BufferedReader data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String ip = null;
        do {
            String[] line = data.readLine().split("=");
            if (line[0].equals("ip")) {
                ip = line[1];
            }
        } while (ip == null);

        data.close();
        return ip;
    }

    public String getIP() throws IOException {
        return getIP("https://1.1.1.1/cdn-cgi/trace");
    }

    public String getIPv6() throws IOException {
        return getIP("https://[2606:4700:4700::1111]/cdn-cgi/trace");
    }

    @Override
    public boolean update(String ip) {
        String zone_id = plugin.getConfig().getString("cloudflare.zone_id");
        String record_id = plugin.getConfig().getString("cloudflare.record_id");
        String token = plugin.getConfig().getString("cloudflare.token");

        try {
            if (ip.isEmpty()) {
                ip = getIP();
            }

            final String finalIp = ip;

            // PATCH isn't supported by HttpURLConnection, HttpRequest requires Java 11, but fine.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.cloudflare.com/client/v4/zones/" + zone_id + "/dns_records/" + record_id))
                    .method("PATCH",
                            HttpRequest.BodyPublishers.ofString(
                                    "{\"content\":\"" + finalIp + "\"}"
                            ))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            Map<?, ?> json = gson.fromJson(response.body(), Map.class);

            if (response.statusCode() == 200 && (Boolean) json.get("success")) {
                plugin.getLogger().info("Updated IP on Cloudflare");
                return true;
            } else {
                plugin.getLogger().warning("Error updating IP on Cloudflare");
                return false;
            }

        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }
}
