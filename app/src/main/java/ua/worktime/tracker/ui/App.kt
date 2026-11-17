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
import androidx.compose.runtime.CompositionLocalProvider
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
import ua.worktime.tracker.data.Str
import java.time.LocalDate

enum class Tab(val icon: ImageVector) {
    HOME(Icons.Filled.Home),
    JOURNAL(Icons.AutoMirrored.Filled.List),
    REPORT(Icons.Filled.DateRange),
    SETTINGS(Icons.Filled.Settings);

    fun title(L: Str): String = when (this) {
        HOME -> L.tabHome
        JOURNAL -> L.tabJournal
        REPORT -> L.tabReport
        SETTINGS -> L.tabSettings
    }
}

/** Стан відкритого редактора зміни. */
data class EditorState(val shift: Shift, val isNew: Boolean)

@Composable
fun App(vm: MainViewModel = viewModel()) {
    val data by vm.data.collectAsStateWithLifecycle()
    val L = Str.of(data.settings.language)
    CompositionLocalProvider(LocalStr provides L) {
        AppContent(vm, data, L)
    }
}

@Composable
private fun AppContent(vm: MainViewModel, data: ua.worktime.tracker.data.AppData, L: Str) {
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
                start = data.settings.defaultStart,
                end = data.settings.defaultEnd,
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
                        label = { Text(t.title(L), maxLines = 1) },
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
                        toast(if (s != null) L.shiftSaved else L.shiftTooShort)
                    },
                    onCancel = { vm.cancelTimer(); toast(L.timerCancelled) },
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
                        if (used > 0) toast(L.rateInUse(used))
                    },
                    onSaveSettings = { vm.saveSettings(it); toast(Str.of(it.language).settingsSaved) },
                    onLanguage = { code -> vm.saveSettings(data.settings.copy(language = code)) },
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
            onSave = { vm.saveShift(it); editor = null; toast(L.saved) },
            onDelete = { vm.deleteShift(it.id); editor = null; toast(L.shiftDeleted) },
        )
    }
}
