package com.alhawiyah.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

class IslandService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var islandView: FrameLayout
    private lateinit var params: WindowManager.LayoutParams
    private var isExpanded = false

    // مقاسات الجزيرة (تم تعديلها عشان تضبط على كاميرا A36)
    private val collapsedWidth = 110f
    private val collapsedHeight = 32f
    private val expandedWidth = 280f
    private val expandedHeight = 80f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        showIsland()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("island_channel", "Island Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            val notification = Notification.Builder(this, "island_channel")
                .setContentTitle("الهاويه تعمل")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build()
            startForeground(1, notification)
        }
    }

    private fun dpToPx(dp: Float): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun showIsland() {
        // شكل الجزيرة (أسود مع شفافية خفيفة)
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadius = dpToPx(20f).toFloat()
        shape.setColor(Color.parseColor("#E6000000")) // أسود بشفافية
        shape.setStroke(dpToPx(1f), Color.parseColor("#33FFFFFF")) // حدود بيضاء خفيفة جداً

        islandView = FrameLayout(this)
        islandView.background = shape
        islandView.elevation = dpToPx(8f).toFloat() // ظل

        // المحتوى الداخلي للجزيرة
        val contentLayout = LinearLayout(this)
        contentLayout.orientation = LinearLayout.HORIZONTAL
        contentLayout.gravity = Gravity.CENTER_VERTICAL
        contentLayout.setPadding(dpToPx(16f), 0, dpToPx(16f), 0)

        val tvIcon = TextView(this)
        tvIcon.text = "🎵"
        tvIcon.textSize = 16f
        
        val tvTitle = TextView(this)
        tvTitle.text = "  يشغل الآن..."
        tvTitle.setTextColor(Color.WHITE)
        tvTitle.textSize = 14f
        tvTitle.setTypeface(null, Typeface.BOLD)
        tvTitle.visibility = View.GONE // مخفي في الحالة الصغيرة

        contentLayout.addView(tvIcon)
        contentLayout.addView(tvTitle)
        islandView.addView(contentLayout)

        // إعدادات النافذة
        params = WindowManager.LayoutParams(
            dpToPx(collapsedWidth),
            dpToPx(collapsedHeight),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = dpToPx(8f) // المسافة من فوق

        windowManager.addView(islandView, params)

        // حدث اللمس
        islandView.setOnClickListener {
            isExpanded = !isExpanded
            animateIsland(isExpanded, tvTitle)
        }
    }

    private fun animateIsland(expand: Boolean, titleText: TextView) {
        val targetWidth = dpToPx(if (expand) expandedWidth else collapsedWidth)
        val targetHeight = dpToPx(if (expand) expandedHeight else collapsedHeight)

        // إخفاء/إظهار النص
        titleText.visibility = if (expand) View.VISIBLE else View.GONE

        // حركة Spring ناعمة جداً (ارتداد خفيف)
        val springX = SpringAnimation(islandView, DynamicAnimation.SCALE_X, 1f)
        val springY = SpringAnimation(islandView, DynamicAnimation.SCALE_Y, 1f)

        // تغيير الحجم الفعلي للنافذة
        val anim = android.animation.ValueAnimator.ofFloat(0f, 1f)
        anim.duration = 350
        anim.addUpdateListener { 
            val fraction = it.animatedFraction
            
            // حساب الحجم الجديد
            params.width = (params.width + (targetWidth - params.width) * 0.3f).toInt()
            params.height = (params.height + (targetHeight - params.height) * 0.3f).toInt()
            
            // تحديث الشكل
            windowManager.updateViewLayout(islandView, params)
        }
        anim.start()
        
        // نستخدم Spring للارتداد (شكلياً ليعطي إحساس النعومة)
        springX.spring = SpringForce(1f).setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
        springY.spring = SpringForce(1f).setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
        springX.start()
        springY.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::islandView.isInitialized) {
            windowManager.removeView(islandView)
        }
    }
}