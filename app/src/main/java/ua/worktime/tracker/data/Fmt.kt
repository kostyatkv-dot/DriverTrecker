package ua.worktime.tracker.data

import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

object Fmt {
    private val UA: Locale = Locale.forLanguageTag("uk-UA")

    val MONTHS = arrayOf(
        "Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень",
        "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень",
    )
    val MONTHS_GEN = arrayOf(
        "січня", "лютого", "березня", "квітня", "травня", "червня",
        "липня", "серпня", "вересня", "жовтня", "листопада", "грудня",
    )
    val DOW_SHORT = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    val DOW_FULL = arrayOf("Понеділок", "Вівторок", "Середа", "Четвер", "П'ятниця", "Субота", "Неділя")

    /** 450 -> "7:30" */
    fun hm(min: Int): String = "%d:%02d".format(min / 60, min % 60)

    /** 450 -> "7 год 30 хв" */
    fun hmLong(min: Int): String {
        val h = min / 60
        val m = min % 60
        return when {
            h == 0 -> "$m хв"
            m == 0 -> "$h год"
            else -> "$h год $m хв"
        }
    }

    fun hoursDec(min: Int): Double = Math.round(min / 60.0 * 100.0) / 100.0

    fun num(v: Double): String = String.format(UA, "%,.2f", v)

    fun money(v: Double, cur: String): String = "${num(v)} $cur"

    fun time(t: LocalTime): String = "%02d:%02d".format(t.hour, t.minute)

    fun date(d: LocalDate): String = "%02d.%02d.%04d".format(d.dayOfMonth, d.monthValue, d.year)

    fun dayTitle(d: LocalDate): String =
        "${DOW_SHORT[d.dayOfWeek.value - 1]}, ${d.dayOfMonth} ${MONTHS_GEN[d.monthValue - 1]}"

    fun dayLong(d: LocalDate): String =
        "${DOW_FULL[d.dayOfWeek.value - 1]}, ${d.dayOfMonth} ${MONTHS_GEN[d.monthValue - 1]} ${d.year}"

    fun month(d: LocalDate): String = "${MONTHS[d.monthValue - 1]} ${d.year}"

    fun period(from: LocalDate, to: LocalDate): String =
        "%02d.%02d – %02d.%02d.%04d".format(from.dayOfMonth, from.monthValue, to.dayOfMonth, to.monthValue, to.year)

    /** Розбір числа з комою або крапкою. */
    fun parseNum(s: String): Double? =
        s.trim().replace(" ", "").replace("\u00A0", "").replace("\u202F", "").replace(',', '.').toDoubleOrNull()

    /** Число для поля вводу: без роздільника тисяч і зайвих нулів (7.5 -> "7,5"). */
    fun plain(v: Double): String =
        java.math.BigDecimal(v).setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString().replace('.', ',')
}
