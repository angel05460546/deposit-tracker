package com.example.deposittracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

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
                if (amount != null) Store.addEntry(context, amount, "واریز", "پیامک")
                continue
            }

            val withdrawMatch = withdrawRegex.find(body)
            if (withdrawMatch != null) {
                val amount = withdrawMatch.groupValues[1].replace(",", "").toLongOrNull()
                if (amount != null) Store.addEntry(context, amount, "برداشت", "پیامک")
            }
        }
    }
}
