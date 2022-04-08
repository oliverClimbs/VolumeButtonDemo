package com.oliverClimbs.volumeButtonDemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.media.MediaPlayer
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import android.util.Log
import com.oliverClimbs.volumeButtonDemo.ForegroundService.Companion.TAG
import com.oliverClimbs.volumeButtonDemo.VolumeButtonHelper.Direction.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VolumeButtonHelper(private var context: Context,
                         private var stream: Int? = null,
                         enabledScreenOff: Boolean)
{
  companion object
  {
    const val VOLUME_CHANGE_ACTION = "android.media.VOLUME_CHANGED_ACTION"
    const val EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"
  }

  enum class Direction
  {
    Up,
    Down,
    Release
  }

  private lateinit var mediaPlayer: MediaPlayer
  private var volumeBroadCastReceiver: VolumeBroadCastReceiver? = null
  private var volumeChangeListener: VolumeChangeListener? = null

  private val audioManager: AudioManager? =
    context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

  private var priorVolume = -1
  private var volumePushes = 0.0
  private var longPressReported = false

  var doublePressTimeout = 350L
  var buttonReleaseTimeout = 100L

  var minVolume = -1
    private set

  var maxVolume = -1
    private set

  var halfVolume = -1
    private set

  var currentVolume = -1
    private set

  // ---------------------------------------------------------------------------------------------
  init
  {
    if (audioManager != null)
    {
      minVolume = audioManager.getStreamMinVolume(STREAM_MUSIC)
      maxVolume = audioManager.getStreamMaxVolume(STREAM_MUSIC)
      halfVolume = (minVolume + maxVolume) / 2

      /*************************************
       * BroadcastReceiver does not get triggered for VOLUME_CHANGE_ACTION
       * if the screen is off and no media is playing.
       * Playing a silent media file solves that.
       *************************************/
      if (enabledScreenOff)
      {
        mediaPlayer =
          MediaPlayer.create(context,
                             R.raw.silence)
//                             R.raw.rapidheartbeat)
            .apply {
              isLooping = true
              setWakeMode(context, PARTIAL_WAKE_LOCK)
              start()

            }
      }
    }
    else
      Log.e(TAG, "Unable to initialize AudioManager")

  }

  // ---------------------------------------------------------------------------------------------
  fun registerVolumeChangeListener(volumeChangeListener: VolumeChangeListener)
  {
    if (volumeBroadCastReceiver == null)
    {
      this.volumeChangeListener = volumeChangeListener
      volumeBroadCastReceiver = VolumeBroadCastReceiver()

      if (volumeBroadCastReceiver != null)
      {
        val filter = IntentFilter()
        filter.addAction(VOLUME_CHANGE_ACTION)

        context.registerReceiver(volumeBroadCastReceiver, filter)

      }
      else
        Log.e(TAG, "Unable to initialize BroadCastReceiver")

    }
  }

  // ---------------------------------------------------------------------------------------------
  fun unregisterReceiver()
  {
    if (volumeBroadCastReceiver != null)
    {
      context.unregisterReceiver(volumeBroadCastReceiver)
      volumeBroadCastReceiver = null

    }
  }

  // ---------------------------------------------------------------------------------------------
  fun onVolumePress(count: Int)
  {
    when (count)
    {
      1 -> volumeChangeListener?.onSinglePress()
      2 -> volumeChangeListener?.onDoublePress()
      else -> volumeChangeListener?.onVolumePress(count)

    }
  }

  // ---------------------------------------------------------------------------------------------
  interface VolumeChangeListener
  {
    fun onVolumeChange(direction: Direction)
    fun onVolumePress(count: Int)
    fun onSinglePress()
    fun onDoublePress()
    fun onLongPress()

  }

  // ---------------------------------------------------------------------------------------------
  inner class VolumeBroadCastReceiver : BroadcastReceiver()
  {
    override fun onReceive(context: Context, intent: Intent)
    {
      if (stream == null ||
          intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1) == stream)
      {
        currentVolume = audioManager?.getStreamVolume(STREAM_MUSIC) ?: -1

        if (currentVolume != -1)
        {
          if (currentVolume != priorVolume)
          {
            if (currentVolume > priorVolume)
              volumeChangeListener?.onVolumeChange(Up)
            else if (currentVolume < priorVolume)
              volumeChangeListener?.onVolumeChange(Down)

            priorVolume = currentVolume

          }

          volumePushes += 0.5 // For some unknown reason (to me), onReceive gets called twice for every button push

          if (volumePushes == 0.5)
          {
            CoroutineScope(Dispatchers.Default).launch {
              delay(doublePressTimeout - buttonReleaseTimeout)
              buttonDown()

            }
          }
        }
      }
    }

    // ---------------------------------------------------------------------------------------------
    private fun buttonDown()
    {
      val startVolumePushes = volumePushes

      CoroutineScope(Dispatchers.Default).launch {
        delay(buttonReleaseTimeout)
        val currentVolumePushes = volumePushes

        if (startVolumePushes != currentVolumePushes)
        {
          if (volumePushes > 2 && !longPressReported)
          {
            longPressReported = true
            volumeChangeListener?.onLongPress()

          }

          buttonDown()

        }
        else
        {
          onVolumePress(volumePushes.toInt())
          volumeChangeListener?.onVolumeChange(Release)
          volumePushes = 0.0
          longPressReported = false

        }
      }
    }
  }
}