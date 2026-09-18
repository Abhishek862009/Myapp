package com.example.countdowndots

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.DisplayMetrics
import android.view.WindowManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Builds today's wallpaper bitmap from whatever is saved in Prefs.
 * Used both by the live in-app preview and by the daily background worker,
 * so "today's dot" is always calculated fresh from the real device date.
 */
object WallpaperGenerator {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun screenSize(ctx: Context): Pair<Int, Int> {
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getMetrics(dm)
        return Pair(dm.widthPixels, dm.heightPixels)
    }

    private fun daysBetween(a: String, b: String): Int {
        val da = sdf.parse(a) ?: return 0
        val db = sdf.parse(b) ?: return 0
        return ((db.time - da.time) / 86400000L).toInt()
    }

    fun todayStr(): String {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return sdf.format(c.time)
    }

    fun generate(ctx: Context, width: Int, height: Int): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val bgColor = Prefs.colorBg(ctx)
        canvas.drawColor(bgColor)

        // background photo, cover-fit
        Prefs.bgImageUri(ctx)?.let { uri ->
            try {
                ctx.contentResolver.openInputStream(uri)?.use { input ->
                    val src = BitmapFactory.decodeStream(input)
                    if (src != null) {
                        val scale = maxOf(width.toFloat() / src.width, height.toFloat() / src.height)
                        val iw = src.width * scale
                        val ih = src.height * scale
                        val left = (width - iw) / 2f
                        val top = (height - ih) / 2f
                        val dst = RectF(left, top, left + iw, top + ih)
                        canvas.drawBitmap(src, null, dst, null)
                    }
                }
            } catch (e: SecurityException) {
                // lost persistable permission on this URI; just skip the photo
            } catch (e: Exception) {
                // corrupt/unreadable image; skip silently, dots still render
            }
        }

        // dark overlay so dots stay legible over a photo
        val dim = Prefs.dimAmount(ctx) / 100f
        if (dim > 0f) {
            val overlay = Paint()
            overlay.color = Color.argb((dim * 255).toInt(), 0, 0, 0)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlay)
        }

        val total = Prefs.totalDays(ctx).coerceIn(1, 400)
        val start = Prefs.startDate(ctx) ?: todayStr()
        val target = Prefs.targetDate(ctx) ?: start
        val today = todayStr()

        var elapsed = daysBetween(start, today)
        elapsed = elapsed.coerceIn(0, total)
        var daysLeft = daysBetween(today, target)
        if (daysLeft < 0) daysLeft = 0

        val gridCols = ceil(sqrt(total * 1.6)).toInt().coerceAtLeast(1)
        val gridRows = ceil(total.toFloat() / gridCols).toInt()

        val marginX = width * 0.12f
        val marginY = height * 0.28f
        val areaW = width - marginX * 2
        val areaH = height - marginY * 2
        val gapPx = (Prefs.prefs(ctx).getInt("dot_gap", 5) / 150f) * areaW
        val cellW = (areaW - gapPx * (gridCols - 1)) / gridCols
        val cellH = (areaH - gapPx * (gridRows - 1)) / gridRows
        val dotSize = min(cellW, cellH)
        val totalGridW = gridCols * dotSize + gapPx * (gridCols - 1)
        val totalGridH = gridRows * dotSize + gapPx * (gridRows - 1)
        val offsetX = marginX + (areaW - totalGridW) / 2
        val offsetY = marginY + (areaH - totalGridH) / 2 + height * 0.05f

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val square = Prefs.shapeSquare(ctx)

        for (i in 0 until total) {
            val col = i % gridCols
            val row = i / gridCols
            val x = offsetX + col * (dotSize + gapPx)
            val y = offsetY + row * (dotSize + gapPx)

            dotPaint.color = when {
                i < elapsed -> Prefs.colorPassed(ctx)
                i == elapsed -> Prefs.colorToday(ctx)
                else -> Prefs.colorRemain(ctx)
            }

            if (square) {
                val r = dotSize * 0.18f
                canvas.drawRoundRect(RectF(x, y, x + dotSize, y + dotSize), r, r, dotPaint)
            } else {
                canvas.drawCircle(x + dotSize / 2, y + dotSize / 2, dotSize / 2, dotPaint)
            }
        }

        if (Prefs.showText(ctx)) {
            val numPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            numPaint.color = Prefs.colorToday(ctx)
            numPaint.textAlign = Paint.Align.CENTER
            numPaint.isFakeBoldText = true
            numPaint.textSize = width * 0.11f
            canvas.drawText(daysLeft.toString(), width / 2f, height * 0.16f, numPaint)

            val lblPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            lblPaint.color = Prefs.colorRemain(ctx)
            lblPaint.alpha = 190
            lblPaint.textAlign = Paint.Align.CENTER
            lblPaint.textSize = width * 0.032f
            val label = if (daysLeft == 1) "DAY LEFT" else "DAYS LEFT"
            canvas.drawText(label, width / 2f, height * 0.16f + width * 0.06f, lblPaint)
        }

        return bmp
    }
}
