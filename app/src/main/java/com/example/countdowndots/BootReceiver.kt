package com.example.countdowndots

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // WorkManager persists periodic work across reboots on its own,
            // but re-scheduling here is a cheap safety net.
            DailyWallpaperWorker.schedule(context)
        }
    }
}
