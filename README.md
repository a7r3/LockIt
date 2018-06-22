# LockIt (WIP)

**Name's not confirmed, yet**

Application which sets a timer, within which the child is allowed to use a restricted set of Apps.

## Features

* (TODO) Apps other than the Whitelisted apps won't be opened

* (TODO) Manage Profiles, which vary in the Apps whitelisted, and the duration of Profile Usage

* Create a Profile with the Apps to be Whitelisted

* (TODO) Trigger a profile with a Wrist-Twist Gesture
  - Can be done using a Foreground Service (Background in API < 26) which constantly awaits for the gesture
  - May Consume battery, would add only if the battery consumption is minimal

# Working

1. On triggering a profile, all apps other than the whitelisted apps won't be allowed to open
   - Whitelisted apps would open
2. Above two restrictions are applied for a given time limit by the parent
3. On crossing the time limit, the Phone is Locked (Sent to Lockscreen)
