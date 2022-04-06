# Volume Button Demo
Demo of using BroadcastReceiver to monitor volume changes.
Since Android 12 broke VolumeProviderCompat's ability to receive volume button events when the screen is off (see https://issuetracker.google.com/issues/201546605), I created the VolumeButtonHelper class based on BroadcastReceiver and associated Service to bring back this functionality (see https://github.com/oliver11111/VolumeButtonDemo/tree/master/app/src/main/java/com/oliverClimbs/volumeButtonDemo)
