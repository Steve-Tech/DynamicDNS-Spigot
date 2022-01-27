package me.stevetech.dynamicdns;

/**
 * @author Steve-Tech
 */

public class DDNSService {

    /**
     * The name of the service, shown in lists of enabled services
     */
    public String name() {
        return "Service";
    }

    /**
     * Should return whether the service is enabled in the config
     */
    public boolean enabled() {
        return false;
    }

    /**
     * Runs any code that should be run during onEnable().
     * @see DynamicDNS#setupServices()
     * @see DynamicDNS#onEnable()
     */
    public void setup() {

    }

    /**
     * Update the IP
     * @param   ip the IP to set manually, usually an empty string, unless set via command.
     * @return  Whether the update was successful
     * @see     DynamicDNS#updateIP(String)
     */
    public boolean update(String ip) {
        return false;
    }
}
