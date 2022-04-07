# Volume Button Demo
Demo of using BroadcastReceiver to monitor volume changes.
Since Android 12 broke VolumeProviderCompat's ability to receive volume button events when the screen is off (see https://issuetracker.google.com/issues/201546605), I created the VolumeButtonHelper class based on BroadcastReceiver and associated Service to bring back this functionality (see https://github.com/oliverClimbs/VolumeButtonDemo/tree/master/app/src/main/java/com/oliverClimbs/volumeButtonDemo)

Please note, since updating the UI from a service is beyond the scope of this demo application, volume change events are just logged to Android Studio's Logcat window.

I encourage anybody using this class to test the VolumeButtonDemo app **thouroughly** before implementing it into their own app.
