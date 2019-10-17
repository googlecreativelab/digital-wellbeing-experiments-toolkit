## Dynamically surface information
![Live Wallpapers](../img/Toolkit_UnlockClock.png)

This directory contains an example project to get started building a Digital Wellbeing experiment with Live Wallpapers. These building block projects could be used as the starting point for a new experiment, a reference for adding new functionality to another app, or just to learn more about the API.

You can learn more about the [WallpaperService API](https://developer.android.com/reference/android/service/wallpaper/WallpaperService) in the developer documentation.

The example project in this folder is the source code for the [Unlock Clock](https://experiments.withgoogle.com/unlock-clock) experiment. It simply maintains a count of how many times a device has been unlocked in a 24 hour period, and displays it on the user's home screen.

This project shows how Live Wallpapers can be used to show data to the user in a subtle, but powerful way. It could be modified to display any other piece of information that could help a user become more aware of their technology usage.

To install this example, you will need to first build the APK (Build > Build Bundles / APK > Build APK). When build has completed, click the popup in the IDE and press 'locate' to show your APK. You can now load this file using ADB at the terminal:

```$ adb install app-debug.apk```