package com.example.countdowndots

import android.app.WallpaperManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Regenerates today's dot-grid wallpaper and applies it with WallpaperManager.
 * WorkManager keeps this scheduled across app restarts and device reboots,
 * so the wallpaper advances by one dot every day with zero manual action.
 */
class DailyWallpaperWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            val (w, h) = WallpaperGenerator.screenSize(applicationContext)
            val bitmap = WallpaperGenerator.generate(applicationContext, w, h)
            val wm = WallpaperManager.getInstance(applicationContext)
            // sets both home screen and lock screen on Android 7+
            wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_NAME = "daily_wallpaper_update"

        /** Call after saving settings, or once at app start, to (re)schedule the daily refresh. */
        fun schedule(ctx: Context) {
            val hour = Prefs.updateHour(ctx)

            val now = Calendar.getInstance()
            val next = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            val initialDelay = next.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DailyWallpaperWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
