package ua.worktime.tracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ua.worktime.tracker.data.ActiveTimer
import ua.worktime.tracker.data.AppData
import ua.worktime.tracker.data.AppSettings
import ua.worktime.tracker.data.Rate
import ua.worktime.tracker.data.Shift
import ua.worktime.tracker.data.Storage
import java.time.Instant
import java.time.ZoneId

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val storage = Storage(app)
    private val _data = MutableStateFlow(storage.load())
    val data: StateFlow<AppData> = _data.asStateFlow()
    private val saveLock = Mutex()

    private fun update(f: (AppData) -> AppData) {
        _data.value = f(_data.value)
        viewModelScope.launch(Dispatchers.IO) {
            saveLock.withLock { storage.save(_data.value) }
        }
    }

    fun startTimer(rateId: String) = update { it.copy(timer = ActiveTimer(System.currentTimeMillis(), rateId)) }

    fun cancelTimer() = update { it.copy(timer = null) }

    /** Зупиняє таймер і зберігає зміну. Повертає збережену зміну або null, якщо вона коротша за хвилину. */
    fun stopTimer(): Shift? {
        val d = _data.value
        val t = d.timer ?: return null
        val zone = ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(t.startMs).atZone(zone)
        val end = Instant.now().atZone(zone)
        val s = Shift(
            date = start.toLocalDate(),
            start = start.toLocalTime().withSecond(0).withNano(0),
            end = end.toLocalTime().withSecond(0).withNano(0),
            rateId = t.rateId,
        )
        val shift = s.copy(breakMin = if (s.totalMin > d.settings.defaultBreakMin) d.settings.defaultBreakMin else 0)
        if (shift.totalMin <= 0) {
            update { it.copy(timer = null) }
            return null
        }
        update { it.copy(timer = null, shifts = it.shifts + shift) }
        return shift
    }

    fun saveShift(s: Shift) = update { d ->
        val exists = d.shifts.any { it.id == s.id }
        d.copy(shifts = if (exists) d.shifts.map { if (it.id == s.id) s else it } else d.shifts + s)
    }

    fun deleteShift(id: String) = update { d -> d.copy(shifts = d.shifts.filterNot { it.id == id }) }

    fun saveRate(r: Rate) = update { d ->
        val exists = d.rates.any { it.id == r.id }
        d.copy(rates = if (exists) d.rates.map { if (it.id == r.id) r else it } else d.rates + r)
    }

    /** Повертає кількість змін, що використовують ставку. Видаляє лише невикористану. */
    fun deleteRate(id: String): Int {
        val used = _data.value.shifts.count { it.rateId == id }
        if (used == 0) update { d -> d.copy(rates = d.rates.filterNot { it.id == id }) }
        return used
    }

    fun saveSettings(s: AppSettings) = update { it.copy(settings = s) }
}
