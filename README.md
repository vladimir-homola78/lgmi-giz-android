# Read Me -  GIZ  Android App "Siegelklarheit" #

**Master** is the current **stable branch** - development work should be done in personal branches and then merge requested.

I'm using Android Studio (0.8 Beta), which uses [gradle](http://www.gradle.org/), but this is **not** a requirement/dependency - everything can be compiled from the command line if necessary.

I'm developing for Android version 4.0.3 (aka Ice Cream Sandwich), which is API level 15 - this means we support 90% of devices (as of [November 2014](https://developer.android.com/about/dashboards/index.html) ).

Build is against SDK API 21 (Android 5).

### Dependencies ###

Just the [v4 support library](http://developer.android.com/tools/support-library/features.html#v4), com.android.support:support-v4:21.0.+.  This is needed for the navigation drawer and tour activity.

## Known Issues/Limitations ##

As well as internet connection, the device requires a **back facing** camera to work.  If it has just a front-facing camera, the app won't work (or at least can't scan).

Accessing the camera hardware is tricky and error-prone, with obscure edge cases.

There's a huge variance of device of display size and resolution, and camera mode and resolutions.  Covering every possible combination is non-trivial.  A **lot** of testing is required on various devices.


## Release ##

Need to run through [ProGuard](http://stackoverflow.com/questions/2446248/remove-all-debug-logging-calls-before-publishing-are-there-tools-to-do-this/2466662#2466662)

Needs signing.

## See Also ##

[Activity Lifecycle](http://developer.android.com/reference/android/app/Activity.html)

[Supporting muliple scrren size](http://developer.android.com/guide/practices/screens_support.html)

[Graphics Pipeline](https://source.android.com/devices/graphics.html)