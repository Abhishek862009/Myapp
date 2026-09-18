package com.example.countdowndots

import android.content.Context
import android.net.Uri

/**
 * Simple wrapper around SharedPreferences for every customizable option.
 * The same values are read by MainActivity (to draw the live preview)
 * and by DailyWallpaperWorker (to redraw + set the wallpaper each day),
 * so this is the single source of truth.
 */
object Prefs {
    private const val NAME = "countdown_dots_prefs"

    private const val KEY_START_DATE = "start_date"      // yyyy-MM-dd
    private const val KEY_TARGET_DATE = "target_date"    // yyyy-MM-dd
    private const val KEY_TOTAL_DAYS = "total_days"
    private const val KEY_COLOR_REMAIN = "color_remain"
    private const val KEY_COLOR_PASSED = "color_passed"
    private const val KEY_COLOR_TODAY = "color_today"
    private const val KEY_COLOR_BG = "color_bg"
    private const val KEY_DIM = "dim_amount"              // 0-90
    private const val KEY_SHOW_TEXT = "show_text"
    private const val KEY_SHAPE_SQUARE = "shape_square"
    private const val KEY_BG_IMAGE_URI = "bg_image_uri"
    private const val KEY_UPDATE_HOUR = "update_hour"     // 0-23, when the daily refresh fires

    fun prefs(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun startDate(ctx: Context) = prefs(ctx).getString(KEY_START_DATE, null)
    fun setStartDate(ctx: Context, v: String) = prefs(ctx).edit().putString(KEY_START_DATE, v).apply()

    fun targetDate(ctx: Context) = prefs(ctx).getString(KEY_TARGET_DATE, null)
    fun setTargetDate(ctx: Context, v: String) = prefs(ctx).edit().putString(KEY_TARGET_DATE, v).apply()

    fun totalDays(ctx: Context) = prefs(ctx).getInt(KEY_TOTAL_DAYS, 100)
    fun setTotalDays(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_TOTAL_DAYS, v).apply()

    fun colorRemain(ctx: Context) = prefs(ctx).getInt(KEY_COLOR_REMAIN, 0xFFFFFFFF.toInt())
    fun setColorRemain(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_COLOR_REMAIN, v).apply()

    fun colorPassed(ctx: Context) = prefs(ctx).getInt(KEY_COLOR_PASSED, 0xFFE4572E.toInt())
    fun setColorPassed(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_COLOR_PASSED, v).apply()

    fun colorToday(ctx: Context) = prefs(ctx).getInt(KEY_COLOR_TODAY, 0xFFFFD23F.toInt())
    fun setColorToday(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_COLOR_TODAY, v).apply()

    fun colorBg(ctx: Context) = prefs(ctx).getInt(KEY_COLOR_BG, 0xFF0A0A0C.toInt())
    fun setColorBg(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_COLOR_BG, v).apply()

    fun dimAmount(ctx: Context) = prefs(ctx).getInt(KEY_DIM, 55)
    fun setDimAmount(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_DIM, v).apply()

    fun showText(ctx: Context) = prefs(ctx).getBoolean(KEY_SHOW_TEXT, true)
    fun setShowText(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean(KEY_SHOW_TEXT, v).apply()

    fun shapeSquare(ctx: Context) = prefs(ctx).getBoolean(KEY_SHAPE_SQUARE, false)
    fun setShapeSquare(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean(KEY_SHAPE_SQUARE, v).apply()

    fun bgImageUri(ctx: Context): Uri? =
        prefs(ctx).getString(KEY_BG_IMAGE_URI, null)?.let { Uri.parse(it) }
    fun setBgImageUri(ctx: Context, v: Uri?) =
        prefs(ctx).edit().putString(KEY_BG_IMAGE_URI, v?.toString()).apply()

    fun updateHour(ctx: Context) = prefs(ctx).getInt(KEY_UPDATE_HOUR, 6) // default 6 AM
    fun setUpdateHour(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_UPDATE_HOUR, v).apply()
}
