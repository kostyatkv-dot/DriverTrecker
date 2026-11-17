package ua.worktime.tracker.data

import ua.worktime.tracker.data.Xlsx.b
import ua.worktime.tracker.data.Xlsx.n
import ua.worktime.tracker.data.Xlsx.nb
import ua.worktime.tracker.data.Xlsx.t
import java.io.OutputStream

object Export {
    fun fileName(L: Str, title: String): String = "${L.fileBase}_${title.replace(' ', '_')}.xlsx"

    fun write(out: OutputStream, summary: Summary, periodTitle: String, s: AppSettings, L: Str) {
        val cur = s.currency
        val pct = Fmt.plain(s.overtimePercent)
        val e = Xlsx.Cell.Empty

        // Аркуш: всі зміни
        val rows = mutableListOf<List<Xlsx.Cell>>()
        rows += listOf(b("${L.xTitle}: $periodTitle (${Fmt.period(summary.from, summary.to)})"))
        rows += emptyList<Xlsx.Cell>()
        rows += listOf(
            b(L.date), b(L.xDay), b(L.start), b(L.end), b(L.xBreak),
            b(L.xWorked), b(L.xRegular), b(L.xOvertime),
            b(L.rate), b(L.xTariff(cur)), b(L.xSum(cur)), b(L.note),
        )
        for (d in summary.days) {
            for (r in d.shifts) {
                rows += listOf(
                    t(Fmt.date(d.date)),
                    t(L.dowShort[d.date.dayOfWeek.value - 1]),
                    t(Fmt.time(r.shift.start)),
                    t(Fmt.time(r.shift.end)),
                    n(r.shift.breakMin.toDouble()),
                    n(r.worked / 60.0),
                    n(r.regular / 60.0),
                    n(r.overtime / 60.0),
                    t(r.rate?.name ?: "—"),
                    n(r.rate?.hourly ?: 0.0),
                    n(r.pay),
                    t(r.shift.note),
                )
            }
        }
        rows += emptyList<Xlsx.Cell>()
        rows += listOf(
            b(L.xTotalRow), e, e, e, e,
            nb(summary.worked / 60.0), nb(summary.regular / 60.0), nb(summary.overtime / 60.0),
            e, e, nb(summary.pay),
        )
        val shifts = Xlsx.Sheet(
            L.xShifts,
            listOf(12.0, 6.0, 9.0, 9.0, 11.0, 18.0, 14.0, 18.0, 16.0, 15.0, 14.0, 24.0),
            rows,
        )

        // Аркуш: підсумок
        val sum = mutableListOf<List<Xlsx.Cell>>()
        sum += listOf(b("${L.xSummary}: $periodTitle"))
        sum += listOf(t(L.xPeriod), t(Fmt.period(summary.from, summary.to)))
        sum += emptyList<Xlsx.Cell>()
        sum += listOf(t(L.workDays), n(summary.daysWorked.toDouble()))
        sum += listOf(t("${L.regularHours}, ${L.h}"), n(summary.regular / 60.0))
        sum += listOf(t("${L.overtimeHours}, ${L.h} (${L.overtimeRule(s)})"), n(summary.overtime / 60.0))
        sum += listOf(b("${L.totalHours}, ${L.h}"), nb(summary.worked / 60.0))
        sum += emptyList<Xlsx.Cell>()
        sum += listOf(t(L.xPayRegular(cur)), n(summary.regularPay))
        sum += listOf(t(L.xPayOvertime(pct, cur)), n(summary.overtimePay))
        sum += listOf(b(L.xPayTotal(cur)), nb(summary.pay))
        sum += emptyList<Xlsx.Cell>()
        sum += listOf(b(L.byRates))
        sum += listOf(
            b(L.rate), b(L.xTariff(cur)), b(L.xRegular), b(L.xOvertime),
            b(L.xRegularMoney(cur)), b(L.xOvertimeMoney(cur)), b(L.xTotalMoney(cur)),
        )
        for (r in summary.byRate) {
            sum += listOf(
                t(r.name), n(r.hourly), n(r.regular / 60.0), n(r.overtime / 60.0),
                n(r.regularPay), n(r.overtimePay), n(r.pay),
            )
        }
        val total = Xlsx.Sheet(L.xSummary, listOf(48.0, 16.0, 14.0, 18.0, 14.0, 18.0, 14.0), sum)

        Xlsx.write(out, listOf(total, shifts))
    }
}
