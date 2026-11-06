package com.tarmedtar.pranktextchanger

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("rules", MODE_PRIVATE) }
    private lateinit var list: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 32, 28, 24) }
        root.addView(TextView(this).apply { text = "Prank Text Changer"; textSize = 26f; setPadding(0,0,0,12) })
        root.addView(TextView(this).apply { text = "أضف عبارة تريد استبدالها بعبارة أخرى. سيظهر البديل كطبقة مرئية فوق النص المطابق في التطبيقات التي تسمح بعرض النصوص." ; textSize=15f })
        val old = EditText(this).apply { hint = "النص الأصلي"; setSingleLine(true) }
        val replacement = EditText(this).apply { hint = "النص البديل"; setSingleLine(true) }
        root.addView(old); root.addView(replacement)
        root.addView(Button(this).apply {
            text = "إضافة قاعدة"
            setOnClickListener {
                val a = old.text.toString().trim(); val b = replacement.text.toString().trim()
                if (a.isEmpty() || b.isEmpty()) { Toast.makeText(this@MainActivity, "أدخل النصين أولاً", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                prefs.edit().putString(a, b).apply(); old.text.clear(); replacement.text.clear(); renderRules()
                Toast.makeText(this@MainActivity, "تمت إضافة القاعدة", Toast.LENGTH_SHORT).show()
            }
        })
        root.addView(Button(this).apply { text = "تفعيل خدمة تغيير النصوص"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } })
        root.addView(TextView(this).apply { text = "فعّل خدمة Prank Text Changer من إعدادات إمكانية الوصول. يمكنك إيقافها في أي وقت من المكان نفسه."; textSize=13f })
        root.addView(TextView(this).apply { text = "قواعد الاستبدال"; textSize=20f; setPadding(0,20,0,8) })
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(list, ViewGroup.LayoutParams(-1, 0).apply { height = ViewGroup.LayoutParams.WRAP_CONTENT })
        setContentView(ScrollView(this).apply { addView(root) })
        renderRules()
    }
    private fun renderRules() {
        list.removeAllViews()
        for ((from, to) in prefs.all) {
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
            row.addView(TextView(this).apply { text = "$from  ←  $to"; textSize = 15f }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(Button(this).apply { text = "حذف"; setOnClickListener { prefs.edit().remove(from).apply(); renderRules() } })
            list.addView(row)
        }
        if (prefs.all.isEmpty()) list.addView(TextView(this).apply { text = "لا توجد قواعد بعد." })
    }
}
