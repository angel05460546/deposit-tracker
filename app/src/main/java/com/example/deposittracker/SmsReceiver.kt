package com.example.deposittracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class SmsReceiver : BroadcastReceiver() {

    private val senderKeyword = ""

    private val depositRegex = Regex("([\\d,]{4,})\\s*ریال[^\\n]{0,40}به حساب شما نشست")
    private val withdrawRegex = Regex("([\\d,]{4,})\\s*ریال[^\\n]{0,40}از حساب شما پرید")

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (msg in messages) {
            val sender = msg.originatingAddress ?: ""
            val body = msg.messageBody ?: ""

            if (senderKeyword.isNotEmpty() && !sender.contains(senderKeyword, true)) continue

            val depositMatch = depositRegex.find(body)
            if (depositMatch != null) {
                val amount = depositMatch.groupValues[1].replace(",", "").toLongOrNull()
                if (amount != null) {
                    Store.addEntry(context, amount, "واریز", "پیامک")
                    notify(context, "واریز جدید", "${fmt(amount)} ریال به حسابتون نشست")
                }
                continue
            }

            val withdrawMatch = withdrawRegex.find(body)
            if (withdrawMatch != null) {
                val amount = withdrawMatch.groupValues[1].replace(",", "").toLongOrNull()
                if (amount != null) {
                    Store.addEntry(context, amount, "برداشت", "پیامک")
                    notify(context, "برداشت جدید", "${fmt(amount)} ریال از حسابتون کم شد")
                }
            }
        }
    }

    private fun fmt(n: Long): String = String.format("%,d", n)

    private fun notify(context: Context, title: String, text: String) {
        val channelId = "deposit_tracker_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "تراکنش‌های بانکی", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // اجازه‌ی نوتیفیکیشن داده نشده؛ داده که ذخیره شد، فقط اعلان نشون داده نمی‌شه
        }
    }
}
