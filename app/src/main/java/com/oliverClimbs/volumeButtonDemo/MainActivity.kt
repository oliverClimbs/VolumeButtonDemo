package com.oliverClimbs.volumeButtonDemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.oliverClimbs.volumeButtonDemo.ForegroundService.Companion.TAG

class MainActivity : AppCompatActivity()
{
  private lateinit var service: Intent

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    service = Intent(ForegroundService.ACTION_FOREGROUND_WAKELOCK).setClass(this,
                                                                            ForegroundService::class.java)

    startService(service)

  }

  override fun onDestroy()
  {
    Log.d(TAG, "MainActivity: onDestroy")
    super.onDestroy()
    stopService(service)

  }
}