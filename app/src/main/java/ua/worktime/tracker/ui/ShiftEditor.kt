package ua.worktime.tracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ua.worktime.tracker.data.AppData
import ua.worktime.tracker.data.Fmt
import ua.worktime.tracker.data.Shift

@Composable
fun ShiftEditorDialog(
    initial: Shift,
    isNew: Boolean,
    data: AppData,
    onDismiss: () -> Unit,
    onSave: (Shift) -> Unit,
    onDelete: (Shift) -> Unit,
) {
    var date by remember { mutableStateOf(initial.date) }
    var start by remember { mutableStateOf(initial.start) }
    var end by remember { mutableStateOf(initial.end) }
    var breakText by remember { mutableStateOf(initial.breakMin.toString()) }
    var rateId by remember { mutableStateOf(initial.rateId) }
    var note by remember { mutableStateOf(initial.note) }

    var pickDate by remember { mutableStateOf(false) }
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val breakMin = breakText.trim().toIntOrNull()
    val draft = initial.copy(
        date = date, start = start, end = end,
        breakMin = (breakMin ?: 0).coerceAtLeast(0), rateId = rateId, note = note.trim(),
    )
    val valid = breakMin != null && breakMin >= 0 && draft.workedMin > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Нова зміна" else "Редагування зміни") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Дата", style = MaterialTheme.typography.labelMedium)
                OutlinedButton(onClick = { pickDate = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(Fmt.dayTitle(date) + " " + date.year)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("Початок", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(onClick = { pickStart = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(Fmt.time(start), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Кінець", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(onClick = { pickEnd = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(Fmt.time(end), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                if (end < start) {
                    Text(
                        "Зміна закінчується наступного дня",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedTextField(
                    value = breakText,
                    onValueChange = { v -> breakText = v.filter { it.isDigit() }.take(4) },
                    label = { Text("Перерва, хв") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (data.rates.isNotEmpty()) {
                    Text("Ставка", style = MaterialTheme.typography.labelMedium)
                    RatePicker(data.rates, rateId, data.settings.currency, { rateId = it }, Modifier.fillMaxWidth())
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Примітка") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Відпрацьовано: ${Fmt.hmLong(draft.workedMin)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                if (!isNew) {
                    TextButton(onClick = { confirmDelete = true }) {
                        Text("Видалити зміну", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft) }, enabled = valid) { Text("Зберегти") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } },
    )

    if (pickDate) DatePickDialog(date, { pickDate = false }) { date = it; pickDate = false }
    if (pickStart) TimePickDialog("Початок зміни", start, { pickStart = false }) { start = it; pickStart = false }
    if (pickEnd) TimePickDialog("Кінець зміни", end, { pickEnd = false }) { end = it; pickEnd = false }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Видалити зміну?") },
            text = { Text("${Fmt.dayTitle(initial.date)}, ${Fmt.time(initial.start)} – ${Fmt.time(initial.end)}") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete(initial) }) {
                    Text("Видалити", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Ні") } },
        )
    }
}
