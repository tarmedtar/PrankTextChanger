package com.tarmedtar.pranktextchanger

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (18 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        fun label(value: String, size: Float = 16f) {
            root.addView(TextView(this).apply { text = value; textSize = size; setPadding(0, pad / 2, 0, pad / 3) })
        }
        fun field(hintText: String, multiline: Boolean = false): EditText {
            return EditText(this).apply {
                hint = hintText
                if (multiline) {
                    minLines = 4
                    gravity = android.view.Gravity.TOP
                    inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                } else {
                    setSingleLine(true)
                }
            }
        }
        label("مغيّر النصوص", 26f)
        label("غيّر أي عبارة داخل النص الذي تلصقه هنا، ثم انسخ النتيجة.")
        label("النص الذي تريد تعديله")
        val source = field("الصق النص هنا…", true)
        root.addView(source, ViewGroup.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        label("ابحث عن العبارة")
        val find = field("العبارة الأصلية")
        root.addView(find)
        label("استبدلها بـ")
        val replacement = field("العبارة الجديدة")
        root.addView(replacement)
        val result = field("ستظهر النتيجة هنا…", true).apply { isFocusable = false; isCursorVisible = false }
        root.addView(Button(this).apply {
            text = "تغيير النص"
            setOnClickListener {
                val old = find.text.toString()
                if (old.isBlank()) {
                    Toast.makeText(this@MainActivity, "اكتب العبارة التي تريد استبدالها", Toast.LENGTH_SHORT).show()
                } else {
                    result.setText(source.text.toString().replace(old, replacement.text.toString(), ignoreCase = false))
                }
            }
        })
        label("النص بعد التغيير")
        root.addView(result, ViewGroup.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(Button(this).apply {
            text = "نسخ النتيجة"
            setOnClickListener {
                val value = result.text.toString()
                if (value.isEmpty()) {
                    Toast.makeText(this@MainActivity, "لا توجد نتيجة لنسخها", Toast.LENGTH_SHORT).show()
                } else {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("النص المعدل", value))
                    Toast.makeText(this@MainActivity, "تم نسخ النص", Toast.LENGTH_SHORT).show()
                }
            }
        })
        label("تعمل هذه النسخة على النص الذي تلصقه داخل التطبيق فقط، ولا تقرأ محتوى التطبيقات الأخرى.")
        setContentView(ScrollView(this).apply { addView(root) })
    }
}
