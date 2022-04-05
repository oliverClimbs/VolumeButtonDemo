package com.oliverClimbs.volumeButtonDemo

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.AudioManager.STREAM_MUSIC
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import android.os.PowerManager.WakeLock
import android.util.Log

class ForegroundService : Service()
{
  private lateinit var volumeButtonHelper: VolumeButtonHelper

  companion object
  {
    var wakeLock: WakeLock? = null

    const val TAG = "VolumeButtonHelper"
    const val ACTION_FOREGROUND_WAKELOCK = "com.oliverClimbs.volumeButtonHelper.FOREGROUND_WAKELOCK"
    const val ACTION_FOREGROUND = "com.oliverClimbs.volumeButtonHelper.FOREGROUND"
    const val WAKELOCK_TAG = "com.oliverClimbs.volumeButtonHelper:wake-service"
    const val CHANNEL_ID = "Running in background"

  }

  override fun onBind(p0: Intent?): IBinder?
  {
    return null
  }

  override fun onCreate()
  {
    Log.i(TAG, "ForegroundService: onCreate")
    super.onCreate()

    volumeButtonHelper = VolumeButtonHelper(this,
                                            STREAM_MUSIC,
                                            enabledScreenOff = true)

    volumeButtonHelper.registerVolumeChangeListener(
      object : VolumeButtonHelper.VolumeChangeListener
      {
        override fun onVolumeChange(direction: VolumeButtonHelper.Direction)
        {
          Log.d(TAG, "onVolumeChange: $direction")
        }

        override fun onVolumePress(count: Int)
        {
          Log.d(TAG, "onVolumePress: $count")
        }

        override fun onSinglePress()
        {
          Log.d(TAG, "onSinglePress")
        }

        override fun onDoublePress()
        {
          Log.d(TAG, "onDoublePress")
        }

        override fun onLongPress()
        {
          Log.d(TAG, "onLongPress")
        }
      })
  }

  @SuppressLint("WakelockTimeout")
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
  {
    super.onStartCommand(intent, flags, startId)

    if (intent?.action == ACTION_FOREGROUND || intent?.action == ACTION_FOREGROUND_WAKELOCK)
    {
      val notification = Notification.Builder(this, CHANNEL_ID).apply {
        setTicker("Status")
        setWhen(System.currentTimeMillis())
        setContentTitle("Volume Button Demo")
        setContentText("Active")
      }.build()

      startForeground(R.string.foreground_service_started,
                      notification)

    }

    if (intent?.action == ACTION_FOREGROUND_WAKELOCK)
    {
      if (wakeLock == null)
      {
        wakeLock = getSystemService(PowerManager::class.java)!!.newWakeLock(
          PARTIAL_WAKE_LOCK,
          WAKELOCK_TAG)

        wakeLock!!.acquire()

      }
      else
      {
        releaseWakeLock()

      }
    }

    return START_STICKY

  }

  private fun releaseWakeLock()
  {
    wakeLock?.release()
    wakeLock = null

  }

  override fun onDestroy()
  {
    Log.i(TAG, "ForegroundService: onDestroy")
    super.onDestroy()
    releaseWakeLock()

    stopForeground(STOP_FOREGROUND_REMOVE)

    volumeButtonHelper.unregisterReceiver()

  }
}