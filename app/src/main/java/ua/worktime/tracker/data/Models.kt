package ua.worktime.tracker.data

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

/** Тариф (ставка) за годину. */
data class Rate(
    val id: String = newId(),
    val name: String,
    val hourly: Double,
)

/** Одна робоча зміна. Якщо кінець раніше за початок — зміна переходить через північ. */
data class Shift(
    val id: String = newId(),
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val breakMin: Int = 0,
    val rateId: String = "",
    val note: String = "",
) {
    /** Тривалість від початку до кінця, хвилини. */
    val totalMin: Int
        get() {
            val s = start.hour * 60 + start.minute
            val e = end.hour * 60 + end.minute
            return if (e >= s) e - s else 24 * 60 - s + e
        }

    /** Відпрацьовано (без перерви), хвилини. */
    val workedMin: Int get() = (totalMin - breakMin).coerceAtLeast(0)
}

data class AppSettings(
    /** Поріг понаднормових на день, хвилини (7 год = 420). 0 — понаднормові не рахуються. */
    val overtimeThresholdMin: Int = 420,
    /** Надбавка за понаднормові, %. */
    val overtimePercent: Double = 25.0,
    /** Перерва за замовчуванням, хвилини. */
    val defaultBreakMin: Int = 0,
    val currency: String = "грн",
    /** Стандартний початок і кінець робочого дня для нових змін. */
    val defaultStart: LocalTime = LocalTime.of(8, 0),
    val defaultEnd: LocalTime = LocalTime.of(17, 0),
    /** Уся робота в суботу / неділю — понаднормова. */
    val saturdayOvertime: Boolean = true,
    val sundayOvertime: Boolean = false,
) {
    fun isOvertimeDay(d: java.time.LocalDate): Boolean = when (d.dayOfWeek) {
        java.time.DayOfWeek.SATURDAY -> saturdayOvertime
        java.time.DayOfWeek.SUNDAY -> sundayOvertime
        else -> false
    }

    /** Короткий опис правила понаднормових для підписів. */
    fun overtimeRule(): String {
        val parts = mutableListOf<String>()
        if (overtimeThresholdMin > 0) parts += "понад ${Fmt.hmLong(overtimeThresholdMin)}/день"
        if (saturdayOvertime && sundayOvertime) parts += "сб і нд"
        else if (saturdayOvertime) parts += "субота"
        else if (sundayOvertime) parts += "неділя"
        return parts.joinToString(", ")
    }
}

/** Запущений таймер зміни. */
data class ActiveTimer(
    val startMs: Long,
    val rateId: String,
)

data class AppData(
    val rates: List<Rate> = emptyList(),
    val shifts: List<Shift> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val timer: ActiveTimer? = null,
) {
    fun rate(id: String): Rate? = rates.firstOrNull { it.id == id }
}
