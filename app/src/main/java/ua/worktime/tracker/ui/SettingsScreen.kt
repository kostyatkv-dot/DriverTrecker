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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ua.worktime.tracker.BuildConfigInfo
import ua.worktime.tracker.data.AppData
import ua.worktime.tracker.data.AppSettings
import ua.worktime.tracker.data.Fmt
import ua.worktime.tracker.data.Rate
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    data: AppData,
    onSaveRate: (Rate) -> Unit,
    onDeleteRate: (String) -> Unit,
    onSaveSettings: (AppSettings) -> Unit,
) {
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
        ScreenTitle("Налаштування")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Ставки (тарифи за годину)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Наприклад: основна, нічна, вихідний день. Ставку обираєте для кожної зміни.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                data.rates.forEach { r ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(r.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (r.hourly > 0) "${Fmt.num(r.hourly)} ${s.currency}/год" else "тариф не вказано",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (r.hourly > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            )
                        }
                        IconButton(onClick = { editRate = r }) { Icon(Icons.Filled.Edit, "Змінити") }
                        IconButton(onClick = { deleteRate = r }) { Icon(Icons.Filled.Delete, "Видалити") }
                    }
                }
                OutlinedButton(
                    onClick = { editRate = Rate(name = "", hourly = 0.0) },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                ) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Додати ставку")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Робочий день", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Цей час підставляється в кожну нову зміну. Його завжди можна змінити під час запису.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("Стандартний початок", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(onClick = { pickStart = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(Fmt.time(defStart), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Стандартний кінець", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(onClick = { pickEnd = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(Fmt.time(defEnd), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Понаднормові та перерви", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = threshold,
                    onValueChange = { threshold = it },
                    label = { Text("Понаднормові після, год на день") },
                    supportingText = { Text("0 — не рахувати понаднормові") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = percent,
                    onValueChange = { percent = it },
                    label = { Text("Надбавка за понаднормові, %") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                SwitchRow("Субота — усі години понаднормові", satOt) { satOt = it }
                SwitchRow("Неділя — усі години понаднормові", sunOt) { sunOt = it }
                OutlinedTextField(
                    value = defBreak,
                    onValueChange = { v -> defBreak = v.filter { it.isDigit() }.take(4) },
                    label = { Text("Перерва за замовчуванням, хв") },
                    supportingText = { Text("Віднімається від кожної зміни (можна змінити в зміні)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it.take(6) },
                    label = { Text("Валюта") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        onSaveSettings(
                            AppSettings(
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
                ) { Text("Зберегти") }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Як рахується", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "• Години понад ${Fmt.hmLong(s.overtimeThresholdMin)} за день — понаднормові, " +
                        "оплачуються за ставкою зміни з надбавкою ${Fmt.plain(s.overtimePercent)}%.\n" +
                        (if (s.saturdayOvertime || s.sundayOvertime)
                            "• Уся робота у ${listOfNotNull(if (s.saturdayOvertime) "суботу" else null, if (s.sundayOvertime) "неділю" else null).joinToString(" і ")} — понаднормова.\n"
                        else "") +
                        "• Якщо змін за день декілька, понаднормовими стають останні години дня.\n" +
                        "• Зміна, що переходить через північ, рахується за датою початку.\n" +
                        "• Сума показується брутто (до утримання податків).",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Версія ${BuildConfigInfo.versionName(androidx.compose.ui.platform.LocalContext.current)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (pickStart) TimePickDialog("Стандартний початок", defStart, { pickStart = false }) { defStart = it; pickStart = false }
    if (pickEnd) TimePickDialog("Стандартний кінець", defEnd, { pickEnd = false }) { defEnd = it; pickEnd = false }

    editRate?.let { r -> RateDialog(r, s.currency, onDismiss = { editRate = null }) { onSaveRate(it); editRate = null } }
    deleteRate?.let { r ->
        AlertDialog(
            onDismissRequest = { deleteRate = null },
            title = { Text("Видалити ставку «${r.name}»?") },
            confirmButton = {
                TextButton(onClick = { onDeleteRate(r.id); deleteRate = null }) {
                    Text("Видалити", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteRate = null }) { Text("Ні") } },
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
    var name by remember { mutableStateOf(initial.name) }
    var hourly by remember {
        mutableStateOf(if (initial.hourly > 0) Fmt.plain(initial.hourly) else "")
    }
    val h = Fmt.parseNum(hourly)
    val valid = name.isNotBlank() && h != null && h >= 0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.name.isEmpty()) "Нова ставка" else "Ставка") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Назва (напр. Основна, Нічна)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = hourly,
                    onValueChange = { hourly = it },
                    label = { Text("Тариф, $currency за годину") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(initial.copy(name = name.trim(), hourly = h ?: 0.0)) }, enabled = valid) {
                Text("Зберегти")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } },
    )
}
