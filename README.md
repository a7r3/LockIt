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

* ~Big TODO: Use Bluetooth/WiFi P2P based communication. Can eliminate the "overkill" requirement of a hosted function.~
  ~- Suggestions are welcome for this. The issue is that neither of this communication methods can be kicked off in the background, easily.~

**Moved to Google Nearby Connections API**

* Manage Permissions
* Manage Disconnection Failures
* Manage Retry Attempts
* Prevent Multiple instances of LockActivity to open up (this was an issue with FCM, when a burst of messages were sent to the device)
* See if LockService advertising forever can be tackled
  
## Working

Android TV (Locked Device) + Phone (Key):
0. (Fresh Start) TV and Phone must connect with each other for the first time, with User side authorization in the UI
0.1 Connections hereon will be made directly with implicit auth (TV and Phone connect with the auth code known only to these devices)
1. Lock can either be started manually in the TV App OR can be done directly from the phone
2. Restricted set of apps will be accessible to the child
3. Parent can unlock the TV device from their phone, with Biometric ID before every Lock/Unlock transaction

Phone (Redevelopment TODO, not present rn):

1. Upon Locking, selected apps won't open up.
2. Above two restrictions are applied for a given time limit by the parent
3. On crossing the time limit, the Phone is Locked (Sent to Lockscreen)

# LICENSE

Project uses the MIT License, you can obtain it at [LICENSE](https://raw.githubusercontent.com/a7r3/LockIt/master/LICENSE)
