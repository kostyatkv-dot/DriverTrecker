package ua.worktime.tracker.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.worktime.tracker.data.AppSettings
import ua.worktime.tracker.data.Fmt
import ua.worktime.tracker.data.Rate
import ua.worktime.tracker.data.Summary
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@Composable
fun WorkTimeTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

val OvertimeColor = Color(0xFFE65100)

@Composable
fun ScreenTitle(text: String, sub: String? = null) {
    Column(Modifier.padding(bottom = 8.dp)) {
        Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (sub != null) {
            Text(sub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StatRow(label: String, value: String, valueColor: Color = Color.Unspecified, bold: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
            textAlign = TextAlign.End,
        )
    }
}

/** Картка з підсумком: години, понаднормові, заробіток. */
@Composable
fun SummaryCard(title: String, sub: String?, s: Summary, settings: AppSettings, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (sub != null) {
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(Modifier.padding(top = 8.dp))
            StatRow("Відпрацьовано", "${Fmt.hm(s.worked)} год")
            if (s.overtime > 0) StatRow("з них понаднормові", "${Fmt.hm(s.overtime)} год", OvertimeColor)
            if (s.hasPay) {
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                StatRow("Заробіток", Fmt.money(s.pay, settings.currency), MaterialTheme.colorScheme.primary, bold = true)
            }
        }
    }
}

/** Заголовок із перемиканням періоду ◀ ▶. */
@Composable
fun PeriodSwitcher(title: String, sub: String?, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Попередній") }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onNext) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Наступний") }
    }
}

fun rateLabel(r: Rate, cur: String): String =
    if (r.hourly > 0) "${r.name} · ${Fmt.num(r.hourly)} $cur/год" else r.name

/** Вибір ставки з випадного списку. */
@Composable
fun RatePicker(rates: List<Rate>, selectedId: String, currency: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    val sel = rates.firstOrNull { it.id == selectedId }
    Box(modifier) {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                sel?.let { rateLabel(it, currency) } ?: if (rates.isEmpty()) "Немає ставок" else "Оберіть ставку",
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.ArrowDropDown, null)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            rates.forEach { r ->
                DropdownMenuItem(
                    text = { Text(rateLabel(r, currency)) },
                    onClick = { onSelect(r.id); open = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickDialog(title: String, initial: LocalTime, onDismiss: () -> Unit, onPick: (LocalTime) -> Unit) {
    val st = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = st) },
        confirmButton = { TextButton(onClick = { onPick(LocalTime.of(st.hour, st.minute)) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickDialog(initial: LocalDate, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
    val st = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val ms = st.selectedDateMillis
                if (ms != null) onPick(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()) else onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } },
    ) {
        DatePicker(state = st)
    }
}
