package ua.worktime.tracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ua.worktime.tracker.data.AppData
import ua.worktime.tracker.data.Calc
import ua.worktime.tracker.data.Fmt
import ua.worktime.tracker.data.Shift
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HomeScreen(
    data: AppData,
    onStart: (String) -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onAddManual: () -> Unit,
    onEdit: (Shift) -> Unit,
) {
    val now by produceState(System.currentTimeMillis(), data.timer) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1000)
        }
    }
    val today = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
    val s = data.settings

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenTitle("Робочий час", Fmt.dayLong(today))

        TimerCard(data, now, onStart, onStop, onCancel)

        val todaySum = Calc.range(data, today, today)
        SummaryCard("Сьогодні", null, todaySum, s)
        todaySum.days.firstOrNull()?.shifts?.let { list ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    list.forEach { r -> ShiftRow(r, data, onClick = { onEdit(r.shift) }) }
                }
            }
        }

        val ws = Calc.weekStart(today)
        SummaryCard("Цей тиждень", Fmt.period(ws, Calc.weekEnd(today)), Calc.range(data, ws, Calc.weekEnd(today)), s)
        SummaryCard(
            "Цей місяць",
            Fmt.month(today),
            Calc.range(data, Calc.monthStart(today), Calc.monthEnd(today)),
            s,
        )

        OutlinedButton(onClick = onAddManual, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Додати зміну вручну")
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TimerCard(
    data: AppData,
    now: Long,
    onStart: (String) -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
) {
    val t = data.timer
    var rateId by rememberSaveable { mutableStateOf(data.rates.firstOrNull()?.id ?: "") }
    if (data.rates.none { it.id == rateId } && data.rates.isNotEmpty()) rateId = data.rates.first().id
    var confirmCancel by remember { mutableStateOf(false) }

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (t != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (t == null) {
                Text("Зміна не розпочата", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                if (data.rates.size > 1) {
                    RatePicker(data.rates, rateId, data.settings.currency, { rateId = it }, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                }
                Button(
                    onClick = { onStart(rateId) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                ) {
                    Icon(Icons.Filled.PlayArrow, null, Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ПОЧАТИ ЗМІНУ", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                val elapsed = ((now - t.startMs) / 1000).coerceAtLeast(0)
                val started = Instant.ofEpochMilli(t.startMs).atZone(ZoneId.systemDefault())
                Text("Зміна триває", style = MaterialTheme.typography.titleMedium)
                Text(
                    "%d:%02d:%02d".format(elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                )
                val dayNote = if (started.toLocalDate() != LocalDate.now()) " (${Fmt.date(started.toLocalDate())})" else ""
                Text("Початок: ${Fmt.time(started.toLocalTime())}$dayNote")
                data.rate(t.rateId)?.let { Text("Ставка: ${rateLabel(it, data.settings.currency)}") }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Filled.Check, null, Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ЗАВЕРШИТИ ЗМІНУ", style = MaterialTheme.typography.titleMedium)
                }
                TextButton(onClick = { confirmCancel = true }) { Text("Скасувати таймер") }
            }
        }
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text("Скасувати таймер?") },
            text = { Text("Поточна зміна не буде збережена.") },
            confirmButton = { TextButton(onClick = { confirmCancel = false; onCancel() }) { Text("Так, скасувати") } },
            dismissButton = { TextButton(onClick = { confirmCancel = false }) { Text("Ні") } },
        )
    }
}

/** Рядок зміни в списку. */
@Composable
fun ShiftRow(r: ua.worktime.tracker.data.ShiftResult, data: AppData, onClick: () -> Unit) {
    val sh = r.shift
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "${Fmt.time(sh.start)} – ${Fmt.time(sh.end)}",
                style = MaterialTheme.typography.titleMedium,
            )
            val parts = mutableListOf<String>()
            if (sh.breakMin > 0) parts += "перерва ${sh.breakMin} хв"
            r.rate?.let { if (data.rates.size > 1) parts += it.name }
            if (sh.note.isNotBlank()) parts += sh.note
            if (parts.isNotEmpty()) {
                Text(
                    parts.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${Fmt.hm(r.worked)} год", fontWeight = FontWeight.Bold)
            if (r.overtime > 0) {
                Text("+${Fmt.hm(r.overtime)} понаднорм.", style = MaterialTheme.typography.bodySmall, color = OvertimeColor)
            }
            if ((r.rate?.hourly ?: 0.0) > 0) {
                Text(
                    Fmt.money(r.pay, data.settings.currency),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
