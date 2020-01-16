
# cosu_lean  
A simple cosu solution  
  
[Based on a google codelabs project](https://codelabs.developers.google.com/codelabs/cosu/index.html?#0)
  
Steps:  
Create a *DevicePolicyManager* inside *MainActivity*  
1. Add a button to the layout, and attach a listener which will:  
    * check if lock task is permitted
    * if not, notify the user
    * if it is, start a LockedActivity intent and finish MainActivity
2. Add *LockedActivity* class and a layout file  
3. Create *DevicePolicyManager* inside *LockedActivity*  
4. Add a button to the layout and attach listener to stop lock task if it’s currently locked, start *MainActivity*, and finish *LockedActivity*
5. Override *onStart* to call *startLockTask* if it’s permitted and if *mActivityManager.getLockTaskModeState()* shows that it’s not locked
6. Enable lock task mode by:
    * [Using a device owner application to whitelist the cosu app for lock task mode](https://codelabs.developers.google.com/codelabs/cosu/index.html?#6)
OR
    * [Make the cosu app have its own Device Admin Receiver](https://codelabs.developers.google.com/codelabs/cosu/index.html?#7)