package com.tarmedtar.pranktextchanger

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView

class TextOverlayService : AccessibilityService() {
    private lateinit var wm: WindowManager
    private val overlays = mutableListOf<TextView>()
    override fun onServiceConnected() { super.onServiceConnected(); wm = getSystemService(WINDOW_SERVICE) as WindowManager }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!::wm.isInitialized) return
        clearOverlays()
        val rules = getSharedPreferences("rules", MODE_PRIVATE).all.mapNotNull { (k,v) -> if (k.isNotEmpty() && v is String) k to v else null }
        if (rules.isEmpty()) return
        val root = rootInActiveWindow ?: return
        scan(root, rules, 0)
    }
    private fun scan(node: AccessibilityNodeInfo, rules: List<Pair<String,String>>, depth: Int) {
        if (depth > 30) return
        val value = node.text?.toString()
        if (node.isVisibleToUser && !node.isPassword && !value.isNullOrBlank()) {
            val match = rules.firstOrNull { value.contains(it.first, ignoreCase = true) }
            if (match != null) {
                val rect = Rect(); node.getBoundsInScreen(rect)
                if (rect.width() > 8 && rect.height() > 8 && overlays.size < 35) addOverlay(rect, value.replace(match.first, match.second, ignoreCase = true))
            }
        }
        for (i in 0 until node.childCount) node.getChild(i)?.let { scan(it, rules, depth + 1) }
    }
    private fun addOverlay(rect: Rect, text: String) {
        val view = TextView(this).apply {
            this.text = text; textSize = 14f; setTextColor(Color.WHITE)
            setBackgroundColor(0xFF30343B.toInt()); gravity = Gravity.CENTER
            setPadding(3, 1, 3, 1); maxLines = 3
        }
        val params = WindowManager.LayoutParams(rect.width().coerceAtLeast(24), rect.height().coerceAtLeast(20),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = rect.left; y = rect.top }
        try { wm.addView(view, params); overlays.add(view) } catch (_: Exception) { }
    }
    private fun clearOverlays() { overlays.toList().forEach { try { wm.removeView(it) } catch (_: Exception) { } }; overlays.clear() }
    override fun onInterrupt() { clearOverlays() }
    override fun onDestroy() { clearOverlays(); super.onDestroy() }
}
