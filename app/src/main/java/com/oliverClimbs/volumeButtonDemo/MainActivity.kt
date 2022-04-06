package com.oliverClimbs.volumeButtonDemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.oliverClimbs.volumeButtonDemo.ForegroundService.Companion.TAG

class MainActivity : AppCompatActivity()
{
  private var configurationChange = false

  // ---------------------------------------------------------------------------------------------
  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (!configurationChange)
      startService(Intent(ForegroundService.ACTION_FOREGROUND_WAKELOCK).setClass(this,
                                                                                 ForegroundService::class.java))

  }

  // ---------------------------------------------------------------------------------------------
  override fun onDestroy()
  {
    Log.d(TAG, "MainActivity: onDestroy")

    configurationChange =
      if (isChangingConfigurations)
        true
      else
      {
        stopService(Intent(this, ForegroundService::class.java))
        false

      }

    super.onDestroy()

  }
}