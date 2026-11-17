package ua.worktime.tracker.data

import java.time.LocalDate
import java.util.Locale

/** Усі тексти інтерфейсу та формати для однієї мови. */
abstract class Str(val code: String, val title: String, private val locale: Locale) {
    abstract val months: Array<String>
    abstract val dowShort: Array<String>
    abstract val dowFull: Array<String>
    abstract fun dayTitle(d: LocalDate): String
    abstract fun dayLong(d: LocalDate): String

    abstract val h: String       // скорочення "година"
    abstract val m: String       // скорочення "хвилина"

    fun hmLong(min: Int): String {
        val hh = min / 60
        val mm = min % 60
        return when {
            hh == 0 -> "$mm $m"
            mm == 0 -> "$hh $h"
            else -> "$hh $h $mm $m"
        }
    }

    fun num(v: Double): String = String.format(locale, "%,.2f", v)
    fun money(v: Double, cur: String): String = "${num(v)} $cur"
    fun month(d: LocalDate): String = "${months[d.monthValue - 1]} ${d.year}"
    fun hours(min: Int): String = "${Fmt.hm(min)} $h"

    // Навігація
    abstract val appTitle: String
    abstract val tabHome: String
    abstract val tabJournal: String
    abstract val tabReport: String
    abstract val tabSettings: String

    // Повідомлення
    abstract val shiftSaved: String
    abstract val shiftTooShort: String
    abstract val timerCancelled: String
    abstract val saved: String
    abstract val shiftDeleted: String
    abstract fun rateInUse(n: Int): String
    abstract val settingsSaved: String
    abstract val excelSaved: String
    abstract val saveError: String

    // Загальні
    abstract val ok: String
    abstract val cancel: String
    abstract val no: String
    abstract val save: String
    abstract val delete: String
    abstract val edit: String
    abstract val prev: String
    abstract val next: String

    // Головна
    abstract val noShift: String
    abstract val startShift: String
    abstract val shiftRunning: String
    abstract val startedAt: String
    abstract val finishShift: String
    abstract val cancelTimer: String
    abstract val cancelTimerQ: String
    abstract val cancelTimerText: String
    abstract val yesCancel: String
    abstract val today: String
    abstract val thisWeek: String
    abstract val thisMonth: String
    abstract val addManual: String

    // Зміни
    abstract fun breakN(min: Int): String
    abstract val overtimeShort: String
    abstract val newShift: String
    abstract val editShift: String
    abstract val date: String
    abstract val start: String
    abstract val end: String
    abstract val nextDay: String
    abstract val breakMin: String
    abstract val rate: String
    abstract val note: String
    abstract val worked: String
    abstract val deleteShift: String
    abstract val deleteShiftQ: String
    abstract val shiftStart: String
    abstract val shiftEnd: String
    abstract val noRates: String
    abstract val chooseRate: String
    abstract val withoutRate: String

    // Підсумки
    abstract val regularHours: String
    abstract val overtimeHours: String
    abstract val totalHours: String
    abstract val payRegular: String
    abstract val payOvertime: String
    abstract val total: String

    // Журнал
    abstract val days: String
    abstract val noEntries: String
    abstract val addShift: String

    // Звіт
    abstract val report: String
    abstract val week: String
    abstract val monthLbl: String
    abstract val hoursTitle: String
    abstract val workDays: String
    abstract val salary: String
    abstract val setRateHint: String
    abstract val byRates: String
    abstract val byDays: String
    abstract val exportExcel: String
    abstract val fileBase: String

    // Налаштування
    abstract val settings: String
    abstract val language: String
    abstract val rates: String
    abstract val ratesHint: String
    abstract val perHour: String
    abstract val rateNotSet: String
    abstract val addRate: String
    abstract val workday: String
    abstract val workdayHint: String
    abstract val defStart: String
    abstract val defEnd: String
    abstract val overtimeAndBreaks: String
    abstract val otAfter: String
    abstract val otAfterHint: String
    abstract val otPercent: String
    abstract val satOt: String
    abstract val sunOt: String
    abstract val defBreak: String
    abstract val defBreakHint: String
    abstract val currency: String
    abstract val howCalc: String
    abstract fun howText(s: AppSettings): String
    abstract val version: String
    abstract fun deleteRateQ(name: String): String
    abstract val newRate: String
    abstract val rateName: String
    abstract fun rateHourly(cur: String): String
    abstract fun overtimeRule(s: AppSettings): String

    // Excel
    abstract val xShifts: String
    abstract val xSummary: String
    abstract val xTitle: String
    abstract val xDay: String
    abstract val xBreak: String
    abstract val xWorked: String
    abstract val xRegular: String
    abstract val xOvertime: String
    abstract fun xTariff(cur: String): String
    abstract fun xSum(cur: String): String
    abstract val xTotalRow: String
    abstract val xPeriod: String
    abstract fun xPayRegular(cur: String): String
    abstract fun xPayOvertime(pct: String, cur: String): String
    abstract fun xPayTotal(cur: String): String
    abstract fun xRegularMoney(cur: String): String
    abstract fun xOvertimeMoney(cur: String): String
    abstract fun xTotalMoney(cur: String): String

    companion object {
        val all: List<Str> get() = listOf(Uk, De)
        fun of(code: String): Str = all.firstOrNull { it.code == code } ?: Uk
    }
}

object Uk : Str("uk", "Українська", Locale.forLanguageTag("uk-UA")) {
    override val months = arrayOf(
        "Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень",
        "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень",
    )
    private val monthsGen = arrayOf(
        "січня", "лютого", "березня", "квітня", "травня", "червня",
        "липня", "серпня", "вересня", "жовтня", "листопада", "грудня",
    )
    override val dowShort = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    override val dowFull = arrayOf("Понеділок", "Вівторок", "Середа", "Четвер", "П'ятниця", "Субота", "Неділя")
    override fun dayTitle(d: LocalDate) = "${dowShort[d.dayOfWeek.value - 1]}, ${d.dayOfMonth} ${monthsGen[d.monthValue - 1]}"
    override fun dayLong(d: LocalDate) = "${dowFull[d.dayOfWeek.value - 1]}, ${d.dayOfMonth} ${monthsGen[d.monthValue - 1]} ${d.year}"
    override val h = "год"
    override val m = "хв"

    override val appTitle = "Робочий час"
    override val tabHome = "Головна"
    override val tabJournal = "Журнал"
    override val tabReport = "Звіт"
    override val tabSettings = "Налаштування"

    override val shiftSaved = "Зміну збережено"
    override val shiftTooShort = "Зміна коротша за хвилину — не збережено"
    override val timerCancelled = "Таймер скасовано"
    override val saved = "Збережено"
    override val shiftDeleted = "Зміну видалено"
    override fun rateInUse(n: Int) = "Ставка використовується у змінах ($n) — спершу змініть їх"
    override val settingsSaved = "Налаштування збережено"
    override val excelSaved = "Файл Excel збережено"
    override val saveError = "Помилка збереження"

    override val ok = "OK"
    override val cancel = "Скасувати"
    override val no = "Ні"
    override val save = "Зберегти"
    override val delete = "Видалити"
    override val edit = "Змінити"
    override val prev = "Попередній"
    override val next = "Наступний"

    override val noShift = "Зміна не розпочата"
    override val startShift = "ПОЧАТИ ЗМІНУ"
    override val shiftRunning = "Зміна триває"
    override val startedAt = "Початок"
    override val finishShift = "ЗАВЕРШИТИ ЗМІНУ"
    override val cancelTimer = "Скасувати таймер"
    override val cancelTimerQ = "Скасувати таймер?"
    override val cancelTimerText = "Поточна зміна не буде збережена."
    override val yesCancel = "Так, скасувати"
    override val today = "Сьогодні"
    override val thisWeek = "Цей тиждень"
    override val thisMonth = "Цей місяць"
    override val addManual = "Додати зміну вручну"

    override fun breakN(min: Int) = "перерва $min хв"
    override val overtimeShort = "понаднорм."
    override val newShift = "Нова зміна"
    override val editShift = "Редагування зміни"
    override val date = "Дата"
    override val start = "Початок"
    override val end = "Кінець"
    override val nextDay = "Зміна закінчується наступного дня"
    override val breakMin = "Перерва, хв"
    override val rate = "Ставка"
    override val note = "Примітка"
    override val worked = "Відпрацьовано"
    override val deleteShift = "Видалити зміну"
    override val deleteShiftQ = "Видалити зміну?"
    override val shiftStart = "Початок зміни"
    override val shiftEnd = "Кінець зміни"
    override val noRates = "Немає ставок"
    override val chooseRate = "Оберіть ставку"
    override val withoutRate = "Без ставки"

    override val regularHours = "Звичайні години"
    override val overtimeHours = "Понаднормові години"
    override val totalHours = "Всього годин"
    override val payRegular = "Оплата за звичайні"
    override val payOvertime = "Оплата за понаднормові"
    override val total = "Всього"

    override val days = "днів"
    override val noEntries = "За цей місяць записів немає.\nНатисніть «+», щоб додати зміну."
    override val addShift = "Додати зміну"

    override val report = "Звіт"
    override val week = "Тиждень"
    override val monthLbl = "Місяць"
    override val hoursTitle = "Години"
    override val workDays = "Робочих днів"
    override val salary = "Заробітна плата (брутто)"
    override val setRateHint = "Вкажіть тариф за годину в «Налаштуваннях», щоб бачити розрахунок зарплати."
    override val byRates = "За ставками"
    override val byDays = "По днях"
    override val exportExcel = "Експорт в Excel"
    override val fileBase = "Робочий_час"

    override val settings = "Налаштування"
    override val language = "Мова"
    override val rates = "Ставки (тарифи за годину)"
    override val ratesHint = "Наприклад: основна, нічна. Ставку обираєте для кожної зміни."
    override val perHour = "год"
    override val rateNotSet = "тариф не вказано"
    override val addRate = "Додати ставку"
    override val workday = "Робочий день"
    override val workdayHint = "Цей час підставляється в кожну нову зміну. Його завжди можна змінити під час запису."
    override val defStart = "Стандартний початок"
    override val defEnd = "Стандартний кінець"
    override val overtimeAndBreaks = "Понаднормові та перерви"
    override val otAfter = "Понаднормові після, год на день"
    override val otAfterHint = "0 — не рахувати понаднормові"
    override val otPercent = "Надбавка за понаднормові, %"
    override val satOt = "Субота — усі години понаднормові"
    override val sunOt = "Неділя — усі години понаднормові"
    override val defBreak = "Перерва за замовчуванням, хв"
    override val defBreakHint = "Віднімається від кожної зміни (можна змінити в зміні)"
    override val currency = "Валюта"
    override val howCalc = "Як рахується"
    override fun howText(s: AppSettings): String {
        val days = listOfNotNull(if (s.saturdayOvertime) "суботу" else null, if (s.sundayOvertime) "неділю" else null)
        return "• Години понад ${hmLong(s.overtimeThresholdMin)} за день — понаднормові, " +
            "оплачуються за ставкою зміни з надбавкою ${Fmt.plain(s.overtimePercent)}%.\n" +
            (if (days.isNotEmpty()) "• Уся робота у ${days.joinToString(" і ")} — понаднормова.\n" else "") +
            "• Якщо змін за день декілька, понаднормовими стають останні години дня.\n" +
            "• Зміна, що переходить через північ, рахується за датою початку.\n" +
            "• Сума показується брутто (до утримання податків)."
    }
    override val version = "Версія"
    override fun deleteRateQ(name: String) = "Видалити ставку «$name»?"
    override val newRate = "Нова ставка"
    override val rateName = "Назва (напр. Основна, Нічна)"
    override fun rateHourly(cur: String) = "Тариф, $cur за годину"
    override fun overtimeRule(s: AppSettings): String {
        val parts = mutableListOf<String>()
        if (s.overtimeThresholdMin > 0) parts += "понад ${hmLong(s.overtimeThresholdMin)}/день"
        if (s.saturdayOvertime && s.sundayOvertime) parts += "сб і нд"
        else if (s.saturdayOvertime) parts += "субота"
        else if (s.sundayOvertime) parts += "неділя"
        return parts.joinToString(", ")
    }

    override val xShifts = "Зміни"
    override val xSummary = "Підсумок"
    override val xTitle = "Робочий час"
    override val xDay = "День"
    override val xBreak = "Перерва, хв"
    override val xWorked = "Відпрацьовано, год"
    override val xRegular = "Звичайні, год"
    override val xOvertime = "Понаднормові, год"
    override fun xTariff(cur: String) = "Тариф, $cur/год"
    override fun xSum(cur: String) = "Сума, $cur"
    override val xTotalRow = "РАЗОМ"
    override val xPeriod = "Період"
    override fun xPayRegular(cur: String) = "Оплата звичайних годин, $cur"
    override fun xPayOvertime(pct: String, cur: String) = "Оплата понаднормових (+$pct%), $cur"
    override fun xPayTotal(cur: String) = "ВСЬОГО НАРАХОВАНО (брутто), $cur"
    override fun xRegularMoney(cur: String) = "Звичайні, $cur"
    override fun xOvertimeMoney(cur: String) = "Понаднормові, $cur"
    override fun xTotalMoney(cur: String) = "Разом, $cur"
}

object De : Str("de", "Deutsch", Locale.GERMANY) {
    override val months = arrayOf(
        "Januar", "Februar", "März", "April", "Mai", "Juni",
        "Juli", "August", "September", "Oktober", "November", "Dezember",
    )
    override val dowShort = arrayOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
    override val dowFull = arrayOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")
    override fun dayTitle(d: LocalDate) = "${dowShort[d.dayOfWeek.value - 1]}, ${d.dayOfMonth}. ${months[d.monthValue - 1]}"
    override fun dayLong(d: LocalDate) = "${dowFull[d.dayOfWeek.value - 1]}, ${d.dayOfMonth}. ${months[d.monthValue - 1]} ${d.year}"
    override val h = "Std."
    override val m = "Min."

    override val appTitle = "Arbeitszeit"
    override val tabHome = "Start"
    override val tabJournal = "Journal"
    override val tabReport = "Bericht"
    override val tabSettings = "Einstellungen"

    override val shiftSaved = "Schicht gespeichert"
    override val shiftTooShort = "Schicht kürzer als eine Minute — nicht gespeichert"
    override val timerCancelled = "Timer abgebrochen"
    override val saved = "Gespeichert"
    override val shiftDeleted = "Schicht gelöscht"
    override fun rateInUse(n: Int) = "Der Satz wird in Schichten verwendet ($n) — bitte zuerst ändern"
    override val settingsSaved = "Einstellungen gespeichert"
    override val excelSaved = "Excel-Datei gespeichert"
    override val saveError = "Fehler beim Speichern"

    override val ok = "OK"
    override val cancel = "Abbrechen"
    override val no = "Nein"
    override val save = "Speichern"
    override val delete = "Löschen"
    override val edit = "Bearbeiten"
    override val prev = "Zurück"
    override val next = "Weiter"

    override val noShift = "Schicht nicht begonnen"
    override val startShift = "SCHICHT STARTEN"
    override val shiftRunning = "Schicht läuft"
    override val startedAt = "Beginn"
    override val finishShift = "SCHICHT BEENDEN"
    override val cancelTimer = "Timer abbrechen"
    override val cancelTimerQ = "Timer abbrechen?"
    override val cancelTimerText = "Die laufende Schicht wird nicht gespeichert."
    override val yesCancel = "Ja, abbrechen"
    override val today = "Heute"
    override val thisWeek = "Diese Woche"
    override val thisMonth = "Dieser Monat"
    override val addManual = "Schicht manuell hinzufügen"

    override fun breakN(min: Int) = "Pause $min Min."
    override val overtimeShort = "Überstd."
    override val newShift = "Neue Schicht"
    override val editShift = "Schicht bearbeiten"
    override val date = "Datum"
    override val start = "Beginn"
    override val end = "Ende"
    override val nextDay = "Die Schicht endet am nächsten Tag"
    override val breakMin = "Pause, Min."
    override val rate = "Satz"
    override val note = "Notiz"
    override val worked = "Gearbeitet"
    override val deleteShift = "Schicht löschen"
    override val deleteShiftQ = "Schicht löschen?"
    override val shiftStart = "Schichtbeginn"
    override val shiftEnd = "Schichtende"
    override val noRates = "Keine Sätze"
    override val chooseRate = "Satz wählen"
    override val withoutRate = "Ohne Satz"

    override val regularHours = "Normalstunden"
    override val overtimeHours = "Überstunden"
    override val totalHours = "Stunden gesamt"
    override val payRegular = "Lohn Normalstunden"
    override val payOvertime = "Lohn Überstunden"
    override val total = "Gesamt"

    override val days = "Tage"
    override val noEntries = "Keine Einträge in diesem Monat.\nTippen Sie auf „+“, um eine Schicht hinzuzufügen."
    override val addShift = "Schicht hinzufügen"

    override val report = "Bericht"
    override val week = "Woche"
    override val monthLbl = "Monat"
    override val hoursTitle = "Stunden"
    override val workDays = "Arbeitstage"
    override val salary = "Lohn (brutto)"
    override val setRateHint = "Geben Sie in den Einstellungen einen Stundenlohn ein, um den Lohn zu berechnen."
    override val byRates = "Nach Sätzen"
    override val byDays = "Nach Tagen"
    override val exportExcel = "Export nach Excel"
    override val fileBase = "Arbeitszeit"

    override val settings = "Einstellungen"
    override val language = "Sprache"
    override val rates = "Stundensätze"
    override val ratesHint = "Zum Beispiel: Normal, Nacht. Der Satz wird für jede Schicht gewählt."
    override val perHour = "Std."
    override val rateNotSet = "kein Satz angegeben"
    override val addRate = "Satz hinzufügen"
    override val workday = "Arbeitstag"
    override val workdayHint = "Diese Zeit wird in jede neue Schicht eingesetzt und kann jederzeit geändert werden."
    override val defStart = "Standard-Beginn"
    override val defEnd = "Standard-Ende"
    override val overtimeAndBreaks = "Überstunden und Pausen"
    override val otAfter = "Überstunden ab, Std. pro Tag"
    override val otAfterHint = "0 — keine Überstunden berechnen"
    override val otPercent = "Überstundenzuschlag, %"
    override val satOt = "Samstag — alle Stunden sind Überstunden"
    override val sunOt = "Sonntag — alle Stunden sind Überstunden"
    override val defBreak = "Standardpause, Min."
    override val defBreakHint = "Wird von jeder Schicht abgezogen (in der Schicht änderbar)"
    override val currency = "Währung"
    override val howCalc = "Berechnung"
    override fun howText(s: AppSettings): String {
        val days = listOfNotNull(if (s.saturdayOvertime) "Samstag" else null, if (s.sundayOvertime) "Sonntag" else null)
        return "• Stunden über ${hmLong(s.overtimeThresholdMin)} pro Tag sind Überstunden und werden mit dem Satz " +
            "der Schicht plus ${Fmt.plain(s.overtimePercent)}% Zuschlag bezahlt.\n" +
            (if (days.isNotEmpty()) "• Jede Arbeit am ${days.joinToString(" und ")} zählt als Überstunden.\n" else "") +
            "• Bei mehreren Schichten am Tag sind die letzten Stunden des Tages Überstunden.\n" +
            "• Eine Schicht über Mitternacht zählt zum Datum des Beginns.\n" +
            "• Beträge sind brutto (vor Steuern und Abgaben)."
    }
    override val version = "Version"
    override fun deleteRateQ(name: String) = "Satz „$name“ löschen?"
    override val newRate = "Neuer Satz"
    override val rateName = "Name (z. B. Normal, Nacht)"
    override fun rateHourly(cur: String) = "Stundenlohn, $cur"
    override fun overtimeRule(s: AppSettings): String {
        val parts = mutableListOf<String>()
        if (s.overtimeThresholdMin > 0) parts += "über ${hmLong(s.overtimeThresholdMin)}/Tag"
        if (s.saturdayOvertime && s.sundayOvertime) parts += "Sa und So"
        else if (s.saturdayOvertime) parts += "Samstag"
        else if (s.sundayOvertime) parts += "Sonntag"
        return parts.joinToString(", ")
    }

    override val xShifts = "Schichten"
    override val xSummary = "Übersicht"
    override val xTitle = "Arbeitszeit"
    override val xDay = "Tag"
    override val xBreak = "Pause, Min."
    override val xWorked = "Gearbeitet, Std."
    override val xRegular = "Normal, Std."
    override val xOvertime = "Überstunden, Std."
    override fun xTariff(cur: String) = "Satz, $cur/Std."
    override fun xSum(cur: String) = "Betrag, $cur"
    override val xTotalRow = "SUMME"
    override val xPeriod = "Zeitraum"
    override fun xPayRegular(cur: String) = "Lohn Normalstunden, $cur"
    override fun xPayOvertime(pct: String, cur: String) = "Lohn Überstunden (+$pct%), $cur"
    override fun xPayTotal(cur: String) = "GESAMT (brutto), $cur"
    override fun xRegularMoney(cur: String) = "Normal, $cur"
    override fun xOvertimeMoney(cur: String) = "Überstunden, $cur"
    override fun xTotalMoney(cur: String) = "Gesamt, $cur"
}
