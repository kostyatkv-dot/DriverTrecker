package ua.worktime.tracker.ui

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ua.worktime.tracker.BuildConfigInfo
import ua.worktime.tracker.data.AppData
import ua.worktime.tracker.data.AppSettings
import ua.worktime.tracker.data.Fmt
import ua.worktime.tracker.data.Rate
import ua.worktime.tracker.data.Str
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    data: AppData,
    onSaveRate: (Rate) -> Unit,
    onDeleteRate: (String) -> Unit,
    onSaveSettings: (AppSettings) -> Unit,
    onLanguage: (String) -> Unit,
) {
    val L = LocalStr.current
    val s = data.settings
    var editRate by remember { mutableStateOf<Rate?>(null) }
    var deleteRate by remember { mutableStateOf<Rate?>(null) }

    var threshold by remember(s) { mutableStateOf(Fmt.plain(s.overtimeThresholdMin / 60.0)) }
    var percent by remember(s) { mutableStateOf(Fmt.plain(s.overtimePercent)) }
    var defBreak by remember(s) { mutableStateOf(s.defaultBreakMin.toString()) }
    var currency by remember(s) { mutableStateOf(s.currency) }
    var defStart by remember(s) { mutableStateOf(s.defaultStart) }
    var defEnd by remember(s) { mutableStateOf(s.defaultEnd) }
    var satOt by remember(s) { mutableStateOf(s.saturdayOvertime) }
    var sunOt by remember(s) { mutableStateOf(s.sundayOvertime) }
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }

    val thrVal = Fmt.parseNum(threshold)
    val pctVal = Fmt.parseNum(percent)
    val brkVal = defBreak.trim().toIntOrNull()
    val valid = thrVal != null && thrVal in 0.0..24.0 && pctVal != null && pctVal >= 0 && brkVal != null && brkVal >= 0 &&
        currency.isNotBlank()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenTitle(L.settings)

        // Мова
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(L.language, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                val langs = Str.all
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    langs.forEachIndexed { i, lang ->
                        SegmentedButton(
                            selected = s.language == lang.code,
                            onClick = { onLanguage(lang.code) },
                            shape = SegmentedButtonDefaults.itemShape(i, langs.size),
                        ) { Text(lang.title) }
                    }
                }
            }
        }

        // Ставки
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(L.rates, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    L.ratesHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                data.rates.forEach { r ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(r.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (r.hourly > 0) "${L.num(r.hourly)} ${s.currency}/${L.perHour}" else L.rateNotSet,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (r.hourly > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            )
                        }
                        IconButton(onClick = { editRate = r }) { Icon(Icons.Filled.Edit, L.edit) }
                        IconButton(onClick = { deleteRate = r }) { Icon(Icons.Filled.Delete, L.delete) }
                    }
                }
                OutlinedButton(
                    onClick = { editRate = Rate(name = "", hourly = 0.0) },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                ) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(L.addRate)
                }
            }
        }

        // Робочий день, понаднормові, перерви
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(L.workday, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    L.workdayHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(L.defStart, style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(onClick = { pickStart = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(Fmt.time(defStart), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(L.defEnd, style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(onClick = { pickEnd = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(Fmt.time(defEnd), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(L.overtimeAndBreaks, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = threshold,
                    onValueChange = { threshold = it },
                    label = { Text(L.otAfter) },
                    supportingText = { Text(L.otAfterHint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = percent,
                    onValueChange = { percent = it },
                    label = { Text(L.otPercent) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                SwitchRow(L.satOt, satOt) { satOt = it }
                SwitchRow(L.sunOt, sunOt) { sunOt = it }
                OutlinedTextField(
                    value = defBreak,
                    onValueChange = { v -> defBreak = v.filter { it.isDigit() }.take(4) },
                    label = { Text(L.defBreak) },
                    supportingText = { Text(L.defBreakHint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it.take(6) },
                    label = { Text(L.currency) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        onSaveSettings(
                            s.copy(
                                overtimeThresholdMin = ((thrVal ?: 7.0) * 60).roundToInt(),
                                overtimePercent = pctVal ?: 25.0,
                                defaultBreakMin = brkVal ?: 0,
                                currency = currency.trim(),
                                defaultStart = defStart,
                                defaultEnd = defEnd,
                                saturdayOvertime = satOt,
                                sundayOvertime = sunOt,
                            )
                        )
                    },
                    enabled = valid,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(L.save) }
            }
        }

        // Пояснення
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(L.howCalc, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(L.howText(s), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${L.version} ${BuildConfigInfo.versionName(LocalContext.current)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (pickStart) TimePickDialog(L.defStart, defStart, { pickStart = false }) { defStart = it; pickStart = false }
    if (pickEnd) TimePickDialog(L.defEnd, defEnd, { pickEnd = false }) { defEnd = it; pickEnd = false }

    editRate?.let { r -> RateDialog(r, s.currency, onDismiss = { editRate = null }) { onSaveRate(it); editRate = null } }
    deleteRate?.let { r ->
        AlertDialog(
            onDismissRequest = { deleteRate = null },
            title = { Text(L.deleteRateQ(r.name)) },
            confirmButton = {
                TextButton(onClick = { onDeleteRate(r.id); deleteRate = null }) {
                    Text(L.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteRate = null }) { Text(L.no) } },
        )
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun RateDialog(initial: Rate, currency: String, onDismiss: () -> Unit, onSave: (Rate) -> Unit) {
    val L = LocalStr.current
    var name by remember { mutableStateOf(initial.name) }
    var hourly by remember { mutableStateOf(if (initial.hourly > 0) Fmt.plain(initial.hourly) else "") }
    val h = Fmt.parseNum(hourly)
    val valid = name.isNotBlank() && h != null && h >= 0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.name.isEmpty()) L.newRate else L.rate) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(L.rateName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = hourly,
                    onValueChange = { hourly = it },
                    label = { Text(L.rateHourly(currency)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(initial.copy(name = name.trim(), hourly = h ?: 0.0)) }, enabled = valid) {
                Text(L.save)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(L.cancel) } },
    )
}
