package ua.worktime.tracker.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

/** Зберігає всі дані в JSON-файлі у внутрішній пам'яті додатку. */
class Storage(ctx: Context) {
    private val dir = ctx.filesDir
    private val file = File(dir, "data.json")

    fun load(): AppData {
        if (!file.exists()) return defaultData()
        return try {
            parse(JSONObject(file.readText()))
        } catch (e: Exception) {
            // Не затираємо пошкоджений файл — зберігаємо копію
            file.copyTo(File(dir, "data.corrupt.${System.currentTimeMillis()}.json"), overwrite = true)
            defaultData()
        }
    }

    fun save(d: AppData) {
        val tmp = File(dir, "data.json.tmp")
        tmp.writeText(toJson(d).toString())
        if (!tmp.renameTo(file)) {
            file.writeText(tmp.readText())
            tmp.delete()
        }
    }

    private fun defaultData() = AppData(rates = listOf(Rate(name = "Основна ставка", hourly = 0.0)))

    private fun toJson(d: AppData): JSONObject {
        val o = JSONObject()
        o.put("version", 1)
        o.put("rates", JSONArray().apply {
            d.rates.forEach { r ->
                put(JSONObject().put("id", r.id).put("name", r.name).put("hourly", r.hourly))
            }
        })
        o.put("shifts", JSONArray().apply {
            d.shifts.forEach { s ->
                put(
                    JSONObject()
                        .put("id", s.id)
                        .put("date", s.date.toString())
                        .put("start", s.start.toString())
                        .put("end", s.end.toString())
                        .put("break", s.breakMin)
                        .put("rate", s.rateId)
                        .put("note", s.note)
                )
            }
        })
        val st = d.settings
        o.put(
            "settings",
            JSONObject()
                .put("otThreshold", st.overtimeThresholdMin)
                .put("otPercent", st.overtimePercent)
                .put("defBreak", st.defaultBreakMin)
                .put("currency", st.currency)
                .put("defStart", st.defaultStart.toString())
                .put("defEnd", st.defaultEnd.toString())
        )
        d.timer?.let { o.put("timer", JSONObject().put("start", it.startMs).put("rate", it.rateId)) }
        return o
    }

    private fun parseTime(s: String, def: LocalTime): LocalTime =
        try { if (s.isBlank()) def else LocalTime.parse(s) } catch (e: Exception) { def }

    private fun parse(o: JSONObject): AppData {
        val rates = mutableListOf<Rate>()
        o.optJSONArray("rates")?.let { a ->
            for (i in 0 until a.length()) {
                val r = a.getJSONObject(i)
                rates += Rate(r.getString("id"), r.optString("name"), r.optDouble("hourly", 0.0))
            }
        }
        val shifts = mutableListOf<Shift>()
        o.optJSONArray("shifts")?.let { a ->
            for (i in 0 until a.length()) {
                val s = a.getJSONObject(i)
                shifts += Shift(
                    id = s.getString("id"),
                    date = LocalDate.parse(s.getString("date")),
                    start = LocalTime.parse(s.getString("start")),
                    end = LocalTime.parse(s.getString("end")),
                    breakMin = s.optInt("break", 0),
                    rateId = s.optString("rate", ""),
                    note = s.optString("note", ""),
                )
            }
        }
        val so = o.optJSONObject("settings")
        val def = AppSettings()
        val settings = if (so == null) def else AppSettings(
            overtimeThresholdMin = so.optInt("otThreshold", def.overtimeThresholdMin),
            overtimePercent = so.optDouble("otPercent", def.overtimePercent),
            defaultBreakMin = so.optInt("defBreak", def.defaultBreakMin),
            currency = so.optString("currency", def.currency),
            defaultStart = parseTime(so.optString("defStart", ""), def.defaultStart),
            defaultEnd = parseTime(so.optString("defEnd", ""), def.defaultEnd),
        )
        val timer = o.optJSONObject("timer")?.let { ActiveTimer(it.getLong("start"), it.optString("rate", "")) }
        return AppData(rates, shifts, settings, timer)
    }
}
