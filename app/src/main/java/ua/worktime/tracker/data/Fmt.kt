package ua.worktime.tracker.data

import java.time.LocalDate
import java.time.LocalTime

/** Формати, що не залежать від мови. */
object Fmt {
    /** 450 -> "7:30" */
    fun hm(min: Int): String = "%d:%02d".format(min / 60, min % 60)

    fun time(t: LocalTime): String = "%02d:%02d".format(t.hour, t.minute)

    fun date(d: LocalDate): String = "%02d.%02d.%04d".format(d.dayOfMonth, d.monthValue, d.year)

    fun period(from: LocalDate, to: LocalDate): String =
        "%02d.%02d – %02d.%02d.%04d".format(from.dayOfMonth, from.monthValue, to.dayOfMonth, to.monthValue, to.year)

    /** Розбір числа з комою або крапкою. */
    fun parseNum(s: String): Double? =
        s.trim().replace(" ", "").replace(" ", "").replace(" ", "").replace(',', '.').toDoubleOrNull()

    /** Число для поля вводу: без роздільника тисяч і зайвих нулів (7.5 -> "7,5"). */
    fun plain(v: Double): String =
        java.math.BigDecimal(v).setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString().replace('.', ',')
}
