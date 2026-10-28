package com.example.deposittracker

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var netText: TextView
    private lateinit var depositText: TextView
    private lateinit var withdrawText: TextView
    private lateinit var permissionButton: Button
    private lateinit var historyContainer: LinearLayout
    private lateinit var amountInput: EditText
    private lateinit var goalAmountInput: EditText
    private lateinit var goalDaysInput: EditText
    private lateinit var goalStatusText: TextView
    private lateinit var goalProgress: ProgressBar

    private val dayMs = 24L * 60 * 60 * 1000

    private val smsPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.POST_NOTIFICATIONS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun buildUi() {
        val pad = dp(20)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, dp(40), pad, pad)
        }

        val titleRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        titleRow.addView(TextView(this).apply {
            text = "دفترچه واریز روزانه"
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        titleRow.addView(Button(this).apply {
            text = "ریست"
            setOnClickListener { confirmReset() }
        })
        root.addView(titleRow)

        permissionButton = Button(this).apply {
            text = "اجازه‌ی خواندن پیامک رو فعال کن"
            setOnClickListener {
                ActivityCompat.requestPermissions(this@MainActivity, smsPermissions, 100)
            }
        }
        root.addView(permissionButton, params(dp(16)))

        netText = TextView(this).apply {
            textSize = 26f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        root.addView(netText, params(dp(24)))

        root.addView(TextView(this).apply {
            text = "موجودی خالص (ریال)"
            textSize = 13f
            gravity = Gravity.CENTER
            alpha = 0.6f
        }, params(dp(2)))

        val statsRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

        val depositBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        depositText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#2E8B57"))
        }
        depositBox.addView(depositText)
        depositBox.addView(TextView(this).apply {
            text = "مجموع واریزی"
            textSize = 11f
            gravity = Gravity.CENTER
            alpha = 0.6f
        })

        val withdrawBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        withdrawText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#C0392B"))
        }
        withdrawBox.addView(withdrawText)
        withdrawBox.addView(TextView(this).apply {
            text = "مجموع برداشت"
            textSize = 11f
            gravity = Gravity.CENTER
            alpha = 0.6f
        })

        statsRow.addView(depositBox)
        statsRow.addView(withdrawBox)
        root.addView(statsRow, params(dp(20)))

        // ---------- برنامه‌ی هدف ----------
        root.addView(TextView(this).apply {
            text = "برنامه‌ی هدف"
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, params(dp(28)))

        goalAmountInput = EditText(this).apply {
            hint = "مبلغ هدف (ریال)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        root.addView(goalAmountInput, params(dp(8)))

        goalDaysInput = EditText(this).apply {
            hint = "مدت (تعداد روز)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        root.addView(goalDaysInput, params(dp(4)))

        val goalButtons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        goalButtons.addView(Button(this).apply {
            text = "ذخیره و شروع از امروز"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { saveGoal() }
        })
        goalButtons.addView(Button(this).apply {
            text = "حذف هدف"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) }
            setOnClickListener {
                Store.clearGoal(this@MainActivity)
                goalAmountInput.setText("")
                goalDaysInput.setText("")
                refresh()
            }
        })
        root.addView(goalButtons, params(dp(8)))

        goalProgress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
        }
        root.addView(goalProgress, params(dp(12)))

        goalStatusText = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.START
            setLineSpacing(0f, 1.3f)
        }
        root.addView(goalStatusText, params(dp(8)))

        // ---------- ثبت دستی ----------
        root.addView(TextView(this).apply {
            text = "ثبت دستی"
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, params(dp(28)))

        amountInput = EditText(this).apply {
            hint = "مبلغ به ریال"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        root.addView(amountInput, params(dp(8)))

        val buttonsRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        buttonsRow.addView(Button(this).apply {
            text = "ثبت واریز"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { addManual("واریز") }
        })
        buttonsRow.addView(Button(this).apply {
            text = "ثبت برداشت"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) }
            setOnClickListener { addManual("برداشت") }
        })
        root.addView(buttonsRow, params(dp(8)))

        root.addView(TextView(this).apply {
            text = "تاریخچه"
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, params(dp(28)))

        historyContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(historyContainer, params(dp(8)))

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun addManual(type: String) {
        val amount = Jalali.normalizeDigits(amountInput.text.toString()).toLongOrNull()
        if (amount != null && amount > 0) {
            Store.addEntry(this, amount, type, "دستی")
            amountInput.setText("")
            refresh()
        } else {
            Toast.makeText(this, "یه مبلغ معتبر وارد کن", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveGoal() {
        val amount = Jalali.normalizeDigits(goalAmountInput.text.toString()).toLongOrNull()
        val days = Jalali.normalizeDigits(goalDaysInput.text.toString()).toIntOrNull()
        if (amount == null || amount <= 0 || days == null || days <= 0) {
            Toast.makeText(this, "مبلغ هدف و تعداد روز رو درست وارد کن", Toast.LENGTH_SHORT).show()
            return
        }
        Store.setGoal(this, amount, days)
        refresh()
        Toast.makeText(this, "برنامه از امروز شروع شد", Toast.LENGTH_SHORT).show()
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle("پاک کردن همه‌ی تراکنش‌ها")
            .setMessage("مطمئنی؟ این کار همه‌ی تاریخچه‌ی واریز و برداشت رو برای همیشه پاک می‌کنه.")
            .setPositiveButton("بله، پاک کن") { _, _ ->
                Store.clearAll(this)
                refresh()
                Toast.makeText(this, "پاک شد", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    private fun refresh() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        permissionButton.visibility = if (hasPermission) View.GONE else View.VISIBLE

        val entries = Store.getEntries(this)
        val totalDeposit = entries.filter { it.type == "واریز" }.sumOf { it.amount }
        val totalWithdraw = entries.filter { it.type == "برداشت" }.sumOf { it.amount }
        val net = totalDeposit - totalWithdraw

        netText.text = fmt(net)
        depositText.text = fmt(totalDeposit)
        withdrawText.text = fmt(totalWithdraw)

        refreshGoal(entries)

        historyContainer.removeAllViews()
        if (entries.isEmpty()) {
            historyContainer.addView(TextView(this).apply {
                text = "هنوز تراکنشی ثبت نشده"
                alpha = 0.6f
            })
        } else {
            entries.sortedByDescending { it.date }.forEach { entry ->
                val sign = if (entry.type == "برداشت") "-" else "+"
                val color = if (entry.type == "برداشت") "#C0392B" else "#2E8B57"
                historyContainer.addView(TextView(this).apply {
                    text = "${Jalali.format(entry.date)}   $sign${fmt(entry.amount)} ریال   (${entry.type} — ${entry.source})"
                    setPadding(0, dp(6), 0, dp(6))
                    setTextColor(Color.parseColor(color))
                })
            }
        }
    }

    private fun refreshGoal(entries: List<Entry>) {
        val goal = Store.getGoal(this)
        if (goal == null) {
            goalProgress.visibility = View.GONE
            goalStatusText.text = "هنوز برنامه‌ای تنظیم نشده.\nمبلغ هدف و تعداد روز رو وارد کن و ذخیره بزن."
            return
        }

        if (goalAmountInput.text.isNullOrBlank()) goalAmountInput.setText(goal.amount.toString())
        if (goalDaysInput.text.isNullOrBlank()) goalDaysInput.setText(goal.days.toString())

        val now = System.currentTimeMillis()
        val startMillis = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(goal.start)?.time ?: now
        val elapsedRaw = ((now - startMillis) / dayMs).toInt() + 1
        val dayIndex = elapsedRaw.coerceIn(1, goal.days)

        // فقط تراکنش‌های بعد از شروع برنامه حساب می‌شن
        val sinceStart = entries.filter { it.date >= goal.start }
        val saved = sinceStart.filter { it.type == "واریز" }.sumOf { it.amount } -
                sinceStart.filter { it.type == "برداشت" }.sumOf { it.amount }

        val daily = Math.ceil(goal.amount.toDouble() / goal.days).toLong()
        val expected = (goal.amount.toDouble() * dayIndex / goal.days).toLong()
        val diff = saved - expected
        val remaining = (goal.amount - saved).coerceAtLeast(0)

        val todayPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val todayNet = entries.filter { it.date.startsWith(todayPrefix) }
            .sumOf { if (it.type == "برداشت") -it.amount else it.amount }

        val percent = ((saved.toDouble() / goal.amount.toDouble()) * 100).coerceIn(0.0, 100.0).toInt()
        goalProgress.visibility = View.VISIBLE
        goalProgress.progress = percent

        val endMillis = startMillis + (goal.days - 1) * dayMs
        val lines = mutableListOf<String>()
        lines.add("هدف: ${fmt(goal.amount)} ریال در ${goal.days} روز")
        lines.add("از ${Jalali.formatMillis(startMillis)} تا ${Jalali.formatMillis(endMillis)}  (روز $dayIndex از ${goal.days})")
        lines.add("جمع‌شده تا الان: ${fmt(saved)} ریال ($percent٪)")
        lines.add("سهم روزانه: ${fmt(daily)} ریال")
        val todayMark = if (todayNet >= daily) "✅" else "⏳"
        lines.add("امروز: ${fmt(todayNet)} از ${fmt(daily)} $todayMark")

        if (saved >= goal.amount) {
            lines.add("🎉 به هدفت رسیدی!")
        } else if (elapsedRaw > goal.days) {
            lines.add("⏰ مدت برنامه تموم شده؛ ${fmt(remaining)} ریال کم داری")
        } else {
            when {
                diff > 0 -> lines.add("✅ ${fmt(diff)} ریال از برنامه جلوتری")
                diff < 0 -> lines.add("⚠️ ${fmt(-diff)} ریال از برنامه عقبی")
                else -> lines.add("👌 دقیقاً روی برنامه‌ای")
            }
            val daysLeft = goal.days - elapsedRaw + 1
            val perDay = Math.ceil(remaining.toDouble() / daysLeft).toLong()
            lines.add("برای رسیدن به هدف از امروز روزی ${fmt(perDay)} ریال لازمه ($daysLeft روز مونده)")
        }

        goalStatusText.text = lines.joinToString("\n")
    }

    private fun fmt(n: Long): String = String.format(Locale.US, "%,d", n)

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun params(top: Int): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.topMargin = top
        return p
    }
}
