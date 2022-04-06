# Volume_Button_Demo
Demo of using BroadcastReceiver to monitor volume changes.
Since Android 12 broke VolumeProviderCompat's ability to receive volume button events when the screen is off, I created this helper class based on BroadcastReceiver and associated Service to bring back this functionality.
