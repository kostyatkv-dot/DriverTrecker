package ua.worktime.tracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ua.worktime.tracker.MainViewModel
import ua.worktime.tracker.data.Shift
import java.time.LocalDate
import java.time.LocalTime

enum class Tab(val title: String, val icon: ImageVector) {
    HOME("Головна", Icons.Filled.Home),
    JOURNAL("Журнал", Icons.AutoMirrored.Filled.List),
    REPORT("Звіт", Icons.Filled.DateRange),
    SETTINGS("Налаштування", Icons.Filled.Settings),
}

/** Стан відкритого редактора зміни. */
data class EditorState(val shift: Shift, val isNew: Boolean)

@Composable
fun App(vm: MainViewModel = viewModel()) {
    val data by vm.data.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var editor by remember { mutableStateOf<EditorState?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun toast(msg: String) {
        scope.launch { snackbar.showSnackbar(msg) }
    }

    fun newShift(date: LocalDate) {
        val rateId = data.rates.firstOrNull()?.id ?: ""
        editor = EditorState(
            Shift(
                date = date,
                start = LocalTime.of(8, 0),
                end = LocalTime.of(16, 0),
                breakMin = data.settings.defaultBreakMin,
                rateId = rateId,
            ),
            isNew = true,
        )
    }

    BackHandler(enabled = tab != Tab.HOME) { tab = Tab.HOME }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, null) },
                        label = { Text(t.title, maxLines = 1) },
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                Tab.HOME -> HomeScreen(
                    data = data,
                    onStart = vm::startTimer,
                    onStop = {
                        val s = vm.stopTimer()
                        toast(if (s != null) "Зміну збережено" else "Зміна коротша за хвилину — не збережено")
                    },
                    onCancel = { vm.cancelTimer(); toast("Таймер скасовано") },
                    onAddManual = { newShift(LocalDate.now()) },
                    onEdit = { editor = EditorState(it, false) },
                )
                Tab.JOURNAL -> JournalScreen(
                    data = data,
                    onAdd = { newShift(it) },
                    onEdit = { editor = EditorState(it, false) },
                )
                Tab.REPORT -> ReportScreen(data = data, toast = ::toast)
                Tab.SETTINGS -> SettingsScreen(
                    data = data,
                    onSaveRate = vm::saveRate,
                    onDeleteRate = { id ->
                        val used = vm.deleteRate(id)
                        if (used > 0) toast("Ставка використовується у змінах ($used) — спершу змініть їх")
                    },
                    onSaveSettings = { vm.saveSettings(it); toast("Налаштування збережено") },
                )
            }
        }
    }

    editor?.let { st ->
        ShiftEditorDialog(
            initial = st.shift,
            isNew = st.isNew,
            data = data,
            onDismiss = { editor = null },
            onSave = { vm.saveShift(it); editor = null; toast("Збережено") },
            onDelete = { vm.deleteShift(it.id); editor = null; toast("Зміну видалено") },
        )
    }
}
