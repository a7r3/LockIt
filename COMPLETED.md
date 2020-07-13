* Let's face the truth: Kids are intelligent enough to remove the app (Android TV doesn't have Device Administrator Service, so we're vulnerable to uninstallation)
  - PackageInstaller is blocked now, gotta make it operable (to allow user to uninstall if insisted)

* ~See if LockService advertising forever can be tackled~ Current implementation works only when device is awake. Still, can look out for some more reduction.
<br>Well, a TV is always awake :shrug:... 

* Use Bluetooth/WiFi P2P based communication. Can eliminate the "overkill" requirement of a hosted function.~
  - Suggestions are welcome for this. The issue is that neither of this communication methods can be kicked off in the background, easily.

> Issue details:
> 
> * ~Moved to Google Nearby Connections API~
>
> Pairing takes considerable time (5 seconds), is juice consuming, and is unreliable (connectivity time changes randomly)
> 
> * ~Moved to Android NsdManager (for discovery of service) + TCP Sockets (for Server)~
> 
> NsdManager's interaction with mdns android daemon works very well (Device Logs and Wireshark Capture Logs say so). But the app isn't informed about any such events at all.
> 
> * **Moved to [Rx2DNSSD](https://github.com/andriydruk/RxDNSSD) + TCP Sockets (Server)**
> 
> Interaction with mdnsd works, App's informed about it, Discovery and Connection takes a few ms! Safe to say we've reached the peak.

* Manage Permissions: Not required in current implementation

* Prevent Multiple instances of LockActivity to open up (this was an issue with FCM, when a burst of messages were sent to the device)


