package com.alhawiyah.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // واجهة أنيقة سوداء
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(Color.parseColor("#121212"))
        layout.gravity = Gravity.CENTER_HORIZONTAL
        layout.setPadding(60, 200, 60, 50)

        val title = TextView(this)
        title.text = "الهاويه 🌌"
        title.setTextColor(Color.WHITE)
        title.textSize = 32f
        title.setTypeface(null, Typeface.BOLD)
        title.gravity = Gravity.CENTER
        layout.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "الجزيرة التفاعلية الخاصة بك"
        subtitle.setTextColor(Color.GRAY)
        subtitle.textSize = 16f
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(0, 20, 0, 100)
        layout.addView(subtitle)

        // سوتش التشغيل
        val switchLayout = LinearLayout(this)
        switchLayout.gravity = Gravity.CENTER_VERTICAL

        val toggle = Switch(this)
        toggle.setText("تفعيل الجزيرة")
        toggle.setTextColor(Color.WHITE)
        toggle.textSize = 18f
        toggle.isChecked = Settings.canDrawOverlays(this)

        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivity(intent)
                    toggle.isChecked = false
                } else {
                    startIslandService()
                }
            } else {
                stopIslandService()
            }
        }
        switchLayout.addView(toggle)
        layout.addView(switchLayout)

        val btnSettings = Button(this)
        btnSettings.text = "الإعدادات المتقدمة ⚙️"
        btnSettings.setBackgroundColor(Color.parseColor("#333333"))
        btnSettings.setTextColor(Color.WHITE)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 80, 0, 0)
        btnSettings.layoutParams = params
        layout.addView(btnSettings)

        setContentView(layout)
    }

    private fun startIslandService() {
        val serviceIntent = Intent(this, IslandService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun stopIslandService() {
        val serviceIntent = Intent(this, IslandService::class.java)
        stopService(serviceIntent)
    }
}