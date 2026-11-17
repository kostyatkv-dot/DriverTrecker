package ua.worktime.tracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.worktime.tracker.data.AppData
import ua.worktime.tracker.data.Calc
import ua.worktime.tracker.data.Fmt
import ua.worktime.tracker.data.Shift
import java.time.LocalDate

@Composable
fun JournalScreen(data: AppData, onAdd: (LocalDate) -> Unit, onEdit: (Shift) -> Unit) {
    var month by rememberSaveable { mutableStateOf(Calc.monthStart(LocalDate.now())) }
    val sum = Calc.range(data, Calc.monthStart(month), Calc.monthEnd(month))
    val L = LocalStr.current

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                PeriodSwitcher(
                    L.month(month),
                    buildString {
                        append("${L.hours(sum.worked)} · ${L.days}: ${sum.daysWorked}")
                        if (sum.overtime > 0) append(" · ${L.overtimeShort} ${Fmt.hm(sum.overtime)}")
                    },
                    onPrev = { month = month.minusMonths(1) },
                    onNext = { month = month.plusMonths(1) },
                )
            }
            if (sum.days.isEmpty()) {
                item {
                    Text(
                        L.noEntries,
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(sum.days.reversed(), key = { it.date.toString() }) { day ->
                Card(Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                L.dayTitle(day.date),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                L.hours(day.worked),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        day.shifts.forEachIndexed { i, r ->
                            if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                            ShiftRow(r, onClick = { onEdit(r.shift) })
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = {
                val today = LocalDate.now()
                onAdd(if (Calc.monthStart(today) == month) today else month)
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, L.addShift)
        }
    }
}
