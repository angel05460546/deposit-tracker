package com.example.deposittracker

import java.util.Calendar
import java.util.Locale

object Jalali {

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 0 until gm2) gDayNo += gDaysInMonth[i]
        if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) gDayNo++
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053
        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461
        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }
        var i = 0
        while (i < 11 && jDayNo >= jDaysInMonth[i]) {
            jDayNo -= jDaysInMonth[i]
            i++
        }
        return Triple(jy, i + 1, jDayNo + 1)
    }

    // ورودی: "yyyy-MM-dd HH:mm:ss" یا "yyyy-MM-dd HH:mm" → خروجی: "1405/07/01  19:28"
    fun format(dateStr: String): String {
        return try {
            val parts = dateStr.substring(0, 10).split("-")
            val time = if (dateStr.length >= 16) dateStr.substring(11, 16) else ""
            val (jy, jm, jd) = gregorianToJalali(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            val d = String.format(Locale.US, "%04d/%02d/%02d", jy, jm, jd)
            if (time.isNotEmpty()) "$d  $time" else d
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatMillis(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val (jy, jm, jd) = gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        return String.format(Locale.US, "%04d/%02d/%02d", jy, jm, jd)
    }

    // اعداد فارسی/عربی رو به انگلیسی تبدیل می‌کنه و ویرگول/فاصله رو حذف می‌کنه
    fun normalizeDigits(s: String): String {
        val sb = StringBuilder()
        for (c in s) {
            when (c) {
                in '۰'..'۹' -> sb.append('0' + (c - '۰'))
                in '٠'..'٩' -> sb.append('0' + (c - '٠'))
                ',', '،', '٬', ' ' -> {}
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }
}
