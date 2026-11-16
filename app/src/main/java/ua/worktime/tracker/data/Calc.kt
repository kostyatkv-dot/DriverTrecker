package ua.worktime.tracker.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class ShiftResult(
    val shift: Shift,
    val rate: Rate?,
    val worked: Int,
    val regular: Int,
    val overtime: Int,
    val regularPay: Double,
    val overtimePay: Double,
) {
    val pay: Double get() = regularPay + overtimePay
}

data class DayResult(val date: LocalDate, val shifts: List<ShiftResult>) {
    val worked: Int get() = shifts.sumOf { it.worked }
    val regular: Int get() = shifts.sumOf { it.regular }
    val overtime: Int get() = shifts.sumOf { it.overtime }
    val pay: Double get() = shifts.sumOf { it.pay }
}

data class RateTotal(
    val name: String,
    val hourly: Double,
    val regular: Int,
    val overtime: Int,
    val regularPay: Double,
    val overtimePay: Double,
) {
    val pay: Double get() = regularPay + overtimePay
}

data class Summary(val from: LocalDate, val to: LocalDate, val days: List<DayResult>) {
    val worked: Int get() = days.sumOf { it.worked }
    val regular: Int get() = days.sumOf { it.regular }
    val overtime: Int get() = days.sumOf { it.overtime }
    val regularPay: Double get() = days.sumOf { d -> d.shifts.sumOf { it.regularPay } }
    val overtimePay: Double get() = days.sumOf { d -> d.shifts.sumOf { it.overtimePay } }
    val pay: Double get() = regularPay + overtimePay
    val daysWorked: Int get() = days.count { it.worked > 0 }
    val hasPay: Boolean get() = days.any { d -> d.shifts.any { (it.rate?.hourly ?: 0.0) > 0.0 } }

    val byRate: List<RateTotal>
        get() = days.flatMap { it.shifts }
            .groupBy { it.rate?.id ?: "" }
            .map { (_, list) ->
                val r = list.first().rate
                RateTotal(
                    name = r?.name ?: "Без ставки",
                    hourly = r?.hourly ?: 0.0,
                    regular = list.sumOf { it.regular },
                    overtime = list.sumOf { it.overtime },
                    regularPay = list.sumOf { it.regularPay },
                    overtimePay = list.sumOf { it.overtimePay },
                )
            }
            .sortedByDescending { it.regular + it.overtime }
}

object Calc {
    /**
     * Розрахунок одного дня. Понаднормові — хвилини понад денний поріг.
     * Якщо змін декілька, понаднормовими стають найпізніші хвилини дня
     * і оплачуються за ставкою тієї зміни, на яку вони припали.
     */
    fun day(date: LocalDate, shifts: List<Shift>, rates: List<Rate>, s: AppSettings): DayResult {
        var acc = 0
        val factor = 1.0 + s.overtimePercent / 100.0
        val results = shifts.sortedBy { it.start }.map { sh ->
            val w = sh.workedMin
            val reg = if (s.overtimeThresholdMin <= 0) w
            else (s.overtimeThresholdMin - acc).coerceIn(0, w)
            val ot = w - reg
            acc += w
            val rate = rates.firstOrNull { it.id == sh.rateId }
            val h = rate?.hourly ?: 0.0
            ShiftResult(
                shift = sh,
                rate = rate,
                worked = w,
                regular = reg,
                overtime = ot,
                regularPay = reg / 60.0 * h,
                overtimePay = ot / 60.0 * h * factor,
            )
        }
        return DayResult(date, results)
    }

    /** Підсумок за період [from; to] включно. Дні без змін не включаються. */
    fun range(data: AppData, from: LocalDate, to: LocalDate): Summary {
        val days = data.shifts
            .filter { !it.date.isBefore(from) && !it.date.isAfter(to) }
            .groupBy { it.date }
            .toSortedMap()
            .map { (d, list) -> day(d, list, data.rates, data.settings) }
        return Summary(from, to, days)
    }

    fun weekStart(d: LocalDate): LocalDate = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    fun weekEnd(d: LocalDate): LocalDate = weekStart(d).plusDays(6)
    fun monthStart(d: LocalDate): LocalDate = d.withDayOfMonth(1)
    fun monthEnd(d: LocalDate): LocalDate = d.withDayOfMonth(d.lengthOfMonth())
}
