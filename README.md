# DuckDNS Spigot  
An unofficial DuckDNS plugin for spigot
### Commands:
* /UpdateIP \[IP\] - Force update the IP to DuckDNS, the IP argument can also be used to overide DuckDNS's IP detection (best to leave it blank)
### Permissions:
* /UpdateIP: DuckDNS.update
* /UpdateIP \[IP\]: DuckDNS.update.ip
### Config:
```yaml
# The Configuration File for Steve's DuckDNS Plugin  
period: 3600 # The period between updating IPs in seconds (3600 = 1 hour)
domain: exampledomain # Your subdomain on DuckDNS.org
token: a7c4d0ad-114e-40ef-ba1d-d217904a50f2 # Your token on DuckDNS.org
```
