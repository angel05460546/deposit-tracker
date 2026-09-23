package com.example.deposittracker

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var netText: TextView
    private lateinit var depositText: TextView
    private lateinit var withdrawText: TextView
    private lateinit var permissionButton: Button
    private lateinit var historyContainer: LinearLayout
    private lateinit var amountInput: EditText

    private val smsPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
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

        root.addView(TextView(this).apply {
            text = "دفترچه واریز روزانه"
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

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
        val amount = amountInput.text.toString().toLongOrNull()
        if (amount != null && amount > 0) {
            Store.addEntry(this, amount, type, "دستی")
            amountInput.setText("")
            refresh()
        } else {
            Toast.makeText(this, "یه مبلغ معتبر وارد کن", Toast.LENGTH_SHORT).show()
        }
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
                    text = "${entry.date}   $sign${fmt(entry.amount)} ریال   (${entry.type} — ${entry.source})"
                    setPadding(0, dp(6), 0, dp(6))
                    setTextColor(Color.parseColor(color))
                })
            }
        }
    }

    private fun fmt(n: Long): String = String.format("%,d", n)

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun params(top: Int): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.topMargin = top
        return p
    }
}
