package com.example.countdowndots

import android.app.DatePickerDialog
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.countdowndots.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displaySdf = SimpleDateFormat("dd MMM yyyy", Locale.US)

    // small curated palette to cycle through per swatch — keeps the UI simple
    // while still letting every colour in the app be fully customised
    private val palette = listOf(
        Color.parseColor("#FFFFFF"), Color.parseColor("#E4572E"), Color.parseColor("#FFD23F"),
        Color.parseColor("#4CC9F0"), Color.parseColor("#7B2CBF"), Color.parseColor("#2ECC71"),
        Color.parseColor("#FF6B9D"), Color.parseColor("#0A0A0C"), Color.parseColor("#1C1B1F"),
        Color.parseColor("#F4F1EA")
    )
    private val hours = listOf(0, 3, 6, 9, 12, 15, 18, 21)

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        try {
            contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) { /* some providers don't support persistable perms; ignore */ }
        Prefs.setBgImageUri(this, uri)
        refreshPreview()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // sensible first-run defaults: start = today, target = +100 days
        if (Prefs.startDate(this) == null) Prefs.setStartDate(this, WallpaperGenerator.todayStr())
        if (Prefs.targetDate(this) == null) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, 100)
            Prefs.setTargetDate(this, sdf.format(c.time))
        }

        binding.inputTotalDays.setText(Prefs.totalDays(this).toString())
        binding.checkSquare.isChecked = Prefs.shapeSquare(this)
        binding.checkShowText.isChecked = Prefs.showText(this)
        binding.seekDim.progress = Prefs.dimAmount(this)
        updateDateButtons()
        updateSwatches()
        updateHourButton()

        binding.btnStartDate.setOnClickListener { pickDate(true) }
        binding.btnTargetDate.setOnClickListener { pickDate(false) }
        binding.btnChoosePhoto.setOnClickListener { pickImage.launch("image/*") }
        binding.btnRemovePhoto.setOnClickListener {
            Prefs.setBgImageUri(this, null)
            refreshPreview()
        }

        binding.swatchRemain.setOnClickListener {
            Prefs.setColorRemain(this, nextColor(Prefs.colorRemain(this))); updateSwatches(); refreshPreview()
        }
        binding.swatchPassed.setOnClickListener {
            Prefs.setColorPassed(this, nextColor(Prefs.colorPassed(this))); updateSwatches(); refreshPreview()
        }
        binding.swatchToday.setOnClickListener {
            Prefs.setColorToday(this, nextColor(Prefs.colorToday(this))); updateSwatches(); refreshPreview()
        }
        binding.swatchBg.setOnClickListener {
            Prefs.setColorBg(this, nextColor(Prefs.colorBg(this))); updateSwatches(); refreshPreview()
        }

        binding.checkSquare.setOnCheckedChangeListener { _, checked ->
            Prefs.setShapeSquare(this, checked); refreshPreview()
        }
        binding.checkShowText.setOnCheckedChangeListener { _, checked ->
            Prefs.setShowText(this, checked); refreshPreview()
        }
        binding.seekDim.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                Prefs.setDimAmount(this@MainActivity, progress); refreshPreview()
            }
            override fun onStartTrackingTouch(sb: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(sb: android.widget.SeekBar?) {}
        })

        binding.btnUpdateHour.setOnClickListener {
            val current = Prefs.updateHour(this)
            val idx = hours.indexOf(current).let { if (it == -1) 0 else it }
            val next = hours[(idx + 1) % hours.size]
            Prefs.setUpdateHour(this, next)
            updateHourButton()
        }

        binding.btnApplyNow.setOnClickListener { saveAndApply() }

        refreshPreview()
    }

    private fun nextColor(current: Int): Int {
        val idx = palette.indexOf(current).let { if (it == -1) -1 else it }
        return palette[(idx + 1) % palette.size]
    }

    private fun pickDate(isStart: Boolean) {
        val cal = Calendar.getInstance()
        val existing = if (isStart) Prefs.startDate(this) else Prefs.targetDate(this)
        existing?.let { sdf.parse(it)?.let { d -> cal.time = d } }

        DatePickerDialog(this, { _, y, m, d ->
            val c = Calendar.getInstance()
            c.set(y, m, d)
            val iso = sdf.format(c.time)
            if (isStart) Prefs.setStartDate(this, iso) else Prefs.setTargetDate(this, iso)
            updateDateButtons()
            refreshPreview()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateButtons() {
        Prefs.startDate(this)?.let { iso ->
            sdf.parse(iso)?.let { binding.btnStartDate.text = "Start: ${displaySdf.format(it)}" }
        }
        Prefs.targetDate(this)?.let { iso ->
            sdf.parse(iso)?.let { binding.btnTargetDate.text = "Target: ${displaySdf.format(it)}" }
        }
    }

    private fun updateSwatches() {
        setSwatch(binding.swatchRemain, Prefs.colorRemain(this))
        setSwatch(binding.swatchPassed, Prefs.colorPassed(this))
        setSwatch(binding.swatchToday, Prefs.colorToday(this))
        setSwatch(binding.swatchBg, Prefs.colorBg(this))
    }

    private fun setSwatch(view: android.view.View, color: Int) {
        val bg = (view.background.mutate() as GradientDrawable)
        bg.setColor(color)
    }

    private fun updateHourButton() {
        val h = Prefs.updateHour(this)
        binding.btnUpdateHour.text = "Update at %02d:00".format(h)
    }

    private fun refreshPreview() {
        val total = binding.inputTotalDays.text.toString().toIntOrNull()?.coerceIn(1, 400) ?: 100
        Prefs.setTotalDays(this, total)
        val (w, h) = WallpaperGenerator.screenSize(this)
        // preview at a smaller size for speed, aspect-matched to the real screen
        val pw = 400
        val ph = (400f * h / w).toInt().coerceAtLeast(200)
        binding.previewImage.setImageBitmap(WallpaperGenerator.generate(this, pw, ph))
    }

    private fun saveAndApply() {
        val total = binding.inputTotalDays.text.toString().toIntOrNull()
        if (total == null || total < 10) {
            Toast.makeText(this, "Total days kam se kam 10 rakho", Toast.LENGTH_SHORT).show()
            return
        }
        Prefs.setTotalDays(this, total)

        val (w, h) = WallpaperGenerator.screenSize(this)
        val bitmap = WallpaperGenerator.generate(this, w, h)
        try {
            val wm = WallpaperManager.getInstance(this)
            wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
            DailyWallpaperWorker.schedule(this)
            Toast.makeText(this, "Wallpaper set! Ab har roz khud update hoga.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Wallpaper set nahi ho paya: ${e.message}", Toast.LENGTH_LONG).show()
        }
        refreshPreview()
    }
}
