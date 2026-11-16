package ua.worktime.tracker.data

import ua.worktime.tracker.data.Xlsx.b
import ua.worktime.tracker.data.Xlsx.n
import ua.worktime.tracker.data.Xlsx.nb
import ua.worktime.tracker.data.Xlsx.t
import java.io.OutputStream

object Export {
    fun fileName(title: String): String = "Робочий_час_${title.replace(' ', '_')}.xlsx"

    fun write(out: OutputStream, summary: Summary, periodTitle: String, s: AppSettings) {
        val cur = s.currency
        val pct = Fmt.plain(s.overtimePercent)

        // Аркуш 1: всі зміни
        val rows = mutableListOf<List<Xlsx.Cell>>()
        rows += listOf(b("Робочий час: $periodTitle (${Fmt.period(summary.from, summary.to)})"))
        rows += emptyList<Xlsx.Cell>()
        rows += listOf(
            b("Дата"), b("День"), b("Початок"), b("Кінець"), b("Перерва, хв"),
            b("Відпрацьовано, год"), b("Звичайні, год"), b("Понаднормові, год"),
            b("Ставка"), b("Тариф, $cur/год"), b("Сума, $cur"), b("Примітка"),
        )
        for (d in summary.days) {
            for (r in d.shifts) {
                rows += listOf(
                    t(Fmt.date(d.date)),
                    t(Fmt.DOW_SHORT[d.date.dayOfWeek.value - 1]),
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
            b("РАЗОМ"), Xlsx.Cell.Empty, Xlsx.Cell.Empty, Xlsx.Cell.Empty, Xlsx.Cell.Empty,
            nb(summary.worked / 60.0), nb(summary.regular / 60.0), nb(summary.overtime / 60.0),
            Xlsx.Cell.Empty, Xlsx.Cell.Empty, nb(summary.pay),
        )
        val shifts = Xlsx.Sheet(
            "Зміни",
            listOf(12.0, 6.0, 9.0, 9.0, 11.0, 18.0, 14.0, 18.0, 16.0, 15.0, 14.0, 24.0),
            rows,
        )

        // Аркуш 2: підсумок
        val sum = mutableListOf<List<Xlsx.Cell>>()
        sum += listOf(b("Підсумок: $periodTitle"))
        sum += listOf(t("Період"), t(Fmt.period(summary.from, summary.to)))
        sum += emptyList<Xlsx.Cell>()
        sum += listOf(t("Робочих днів"), n(summary.daysWorked.toDouble()))
        sum += listOf(t("Відпрацьовано, год"), n(summary.worked / 60.0))
        sum += listOf(t("Звичайні години"), n(summary.regular / 60.0))
        sum += listOf(t("Понаднормові години (понад ${Fmt.hmLong(s.overtimeThresholdMin)}/день)"), n(summary.overtime / 60.0))
        sum += emptyList<Xlsx.Cell>()
        sum += listOf(t("Оплата звичайних годин, $cur"), n(summary.regularPay))
        sum += listOf(t("Оплата понаднормових (+$pct%), $cur"), n(summary.overtimePay))
        sum += listOf(b("ВСЬОГО НАРАХОВАНО (брутто), $cur"), nb(summary.pay))
        sum += emptyList<Xlsx.Cell>()
        sum += listOf(b("За ставками"))
        sum += listOf(
            b("Ставка"), b("Тариф, $cur/год"), b("Звичайні, год"), b("Понаднормові, год"),
            b("Звичайні, $cur"), b("Понаднормові, $cur"), b("Разом, $cur"),
        )
        for (r in summary.byRate) {
            sum += listOf(
                t(r.name), n(r.hourly), n(r.regular / 60.0), n(r.overtime / 60.0),
                n(r.regularPay), n(r.overtimePay), n(r.pay),
            )
        }
        val total = Xlsx.Sheet("Підсумок", listOf(44.0, 16.0, 14.0, 18.0, 14.0, 18.0, 14.0), sum)

        Xlsx.write(out, listOf(total, shifts))
    }
}
