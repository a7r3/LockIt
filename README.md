# LockIt

Digital Well-being for Android TVs, managed!

[Phone Remote Locker app (WiLock) is available here](https://github.com/a7r3/WiLock)

Android-TV + Phone: Application which locks down selected apps for a child, which can only be unlocked from a Parent's device.

Phone (Redevelopment TODO): Application which sets a timer, within which the child is allowed to use a restricted set of Apps

## ... Why?

With the day-to-day work by parents, it becomes a difficulty to manage their child's device usage. When given a device with the child assuring "I'll play for 5 mins plsplspls", it usually doesn't happen that way, and the child ends up using for hours :laugh:.

Now, coming to Android TVs, popular App Lock solutions require to enter a PIN in the big picture. Let's not underestimate a child's image processing abilities to pick up the PIN.

## Features

* Android TV + Phone: Remotely Lock/Unlock Android TV device. No requirement to enter PIN in the TV.

* Phone: Decide the time within which child can use restricted apps. Phone Locks once time's over.

## Issues / TODO

* Let's face the truth: Kids are intelligent enough to remove the app (Android TV doesn't have Device Administrator Service, so we're vulnerable to uninstallation)

* Issue when the remote locker device is lost OR the the remote locker app is uninstalled
  - How to get back in the system? Something to think on

* Manage Disconnection Failures

* ~See if LockService advertising forever can be tackled~ Current implementation works only when device is awake. Still, can look out for some more reduction.
<br>Well, a TV is always awake :shrug:... 

* ~Big TODO: Use Bluetooth/WiFi P2P based communication. Can eliminate the "overkill" requirement of a hosted function.~
  ~- Suggestions are welcome for this. The issue is that neither of this communication methods can be kicked off in the background, easily.~

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

* ~Authentication~ Basic done

* ~Manage Permissions~ Not required in current implementation

* ~Prevent Multiple instances of LockActivity to open up (this was an issue with FCM, when a burst of messages were sent to the device)~

## Working

Android TV (Locked Device) + Phone (Key):

Fresh Start:
1. (Fresh Start) TV and Phone must connect with each other for the first time. Connections to the TV are open until the remote client connects to it.
2. Connections hereon will be made directly with the UUID known to the device pair

After Fresh Start:
1. Lock can either be started manually in the TV App OR can be done directly from the phone
2. Restricted set of apps will be accessible to the child
3. Parent can unlock the TV device from their phone, with Biometric ID before every Lock/Unlock transaction

Phone (Redevelopment TODO, not present rn):

1. Upon Locking, selected apps won't open up.
2. Above two restrictions are applied for a given time limit by the parent
3. On crossing the time limit, the Phone is Locked (Sent to Lockscreen)

# LICENSE

Project uses the MIT License, you can obtain it at [LICENSE](https://raw.githubusercontent.com/a7r3/LockIt/master/LICENSE)
