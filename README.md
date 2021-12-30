# DynamicDNS for Spigot  
A dynamic DNS plugin for Spigot
### Commands:
* /UpdateIP \[IP\] - Force updates the IP on your DDNS Services, the IP argument can also be used to overide the IP detection on the DDNS services (best to leave it blank)
### Permissions:
* /UpdateIP: DynamicDNS.update
* /UpdateIP \[IP\]: DynamicDNS.update.ip
### Config:
```yaml
# The Configuration File for Steve's DynamicDNS Plugin  
period: 3600 # The period between updating IPs in seconds (3600 = 1 hour)

afraid:
  enabled: false
  token: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx # The token taken from the URL on the Dynamic DNS page (eg. http://freedns.afraid.org/dynamic/update.php?xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx)

duckdns:
  enabled: false
  domain: exampledomain # Your subdomain on DuckDNS.org
  token: a7c4d0ad-114e-40ef-ba1d-d217904a50f2 # Your token on DuckDNS.org

dynu:
  enabled: false
  hostname: example.ddnsfree.com # Your domain on Dynu
  password: password as plaintext, md5 or sha256 # Your password for Dynu (preferably hashed as md5 or sha256 so you're not storing your password in this file)

noip:
  enabled: false
  hostname: example.ddnsfree.com # Your domain on No-IP
  username: username # The username to your account
  password: password # The password to your account (sadly they don't support hashed passwords)

namecheap
  enabled: false
  subdomain: subdomain of server, use @ if none
  domain: exampledomain.com
  token: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx # The token under the "Advanced DNS" Section in your namecheap domain list

custom:
  enabled: false
  url: https://example.com/?ip=%ip% # The URL to your custom DDNS service, %ip% will be replaced with an ip given as an argument to /updateip [IP]
```
