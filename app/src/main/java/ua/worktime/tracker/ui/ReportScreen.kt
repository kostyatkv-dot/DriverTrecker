package ua.worktime.tracker.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.worktime.tracker.data.AppData
import ua.worktime.tracker.data.Calc
import ua.worktime.tracker.data.Export
import ua.worktime.tracker.data.Fmt
import java.time.LocalDate

private const val XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(data: AppData, toast: (String) -> Unit) {
    var monthly by rememberSaveable { mutableStateOf(true) }
    var anchor by rememberSaveable { mutableStateOf(LocalDate.now()) }
    val from = if (monthly) Calc.monthStart(anchor) else Calc.weekStart(anchor)
    val to = if (monthly) Calc.monthEnd(anchor) else Calc.weekEnd(anchor)
    val sum = Calc.range(data, from, to)
    val s = data.settings
    val L = LocalStr.current
    val title = if (monthly) L.month(from) else "${L.week} ${Fmt.period(from, to)}"
    val fileTitle = if (monthly) "%04d-%02d".format(from.year, from.monthValue) else "${L.week}_${from}"

    val ctx = LocalContext.current
    val currentSum by rememberUpdatedState(sum)
    val currentTitle by rememberUpdatedState(title)
    val currentSettings by rememberUpdatedState(s)
    val currentL by rememberUpdatedState(L)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(XLSX_MIME)) { uri ->
        if (uri != null) {
            try {
                ctx.contentResolver.openOutputStream(uri)?.use { out ->
                    Export.write(out, currentSum, currentTitle, currentSettings, currentL)
                }
                toast(currentL.excelSaved)
            } catch (e: Exception) {
                toast("${currentL.saveError}: ${e.message}")
            }
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenTitle(L.report)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = !monthly,
                onClick = { monthly = false },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
            ) { Text(L.week) }
            SegmentedButton(
                selected = monthly,
                onClick = { monthly = true },
                shape = SegmentedButtonDefaults.itemShape(1, 2),
            ) { Text(L.monthLbl) }
        }
        PeriodSwitcher(
            if (monthly) L.month(from) else L.week,
            Fmt.period(from, to),
            onPrev = { anchor = if (monthly) anchor.minusMonths(1) else anchor.minusWeeks(1) },
            onNext = { anchor = if (monthly) anchor.plusMonths(1) else anchor.plusWeeks(1) },
        )

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(L.hoursTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                StatRow(L.workDays, sum.daysWorked.toString())
                StatRow(L.regularHours, L.hours(sum.regular))
                StatRow(
                    L.overtimeHours,
                    L.hours(sum.overtime),
                    if (sum.overtime > 0) OvertimeColor else androidx.compose.ui.graphics.Color.Unspecified,
                )
                Text(
                    "(${L.overtimeRule(s)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatRow(L.totalHours, "${Fmt.hm(sum.worked)} (${L.num(sum.worked / 60.0)} ${L.h})", bold = true)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(L.salary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                if (!sum.hasPay) {
                    Text(
                        L.setRateHint,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    StatRow(L.payRegular, L.money(sum.regularPay, s.currency))
                    StatRow(
                        "${L.payOvertime} (+${Fmt.plain(s.overtimePercent)}%)",
                        L.money(sum.overtimePay, s.currency),
                        if (sum.overtimePay > 0) OvertimeColor else androidx.compose.ui.graphics.Color.Unspecified,
                    )
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    StatRow(L.total, L.money(sum.pay, s.currency), MaterialTheme.colorScheme.primary, bold = true)
                    val rates = sum.byRate
                    if (rates.size > 1) {
                        Spacer(Modifier.height(10.dp))
                        Text(L.byRates, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        rates.forEach { r ->
                            StatRow(
                                "${r.name} (${L.hours(r.regular + r.overtime)})",
                                L.money(r.pay, s.currency),
                            )
                        }
                    }
                }
            }
        }

        if (sum.days.isNotEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(L.byDays, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    sum.days.forEach { d ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(L.dayTitle(d.date), Modifier.weight(1f))
                            Text(L.hours(d.worked), fontWeight = FontWeight.Medium)
                            if (d.overtime > 0) {
                                Text("  +${Fmt.hm(d.overtime)}", color = OvertimeColor, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { launcher.launch(Export.fileName(L, fileTitle)) },
            enabled = sum.days.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(Icons.Filled.Share, null)
            Spacer(Modifier.width(8.dp))
            Text(L.exportExcel)
        }
        Spacer(Modifier.height(8.dp))
    }
}
