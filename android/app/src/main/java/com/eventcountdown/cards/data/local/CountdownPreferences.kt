package com.eventcountdown.cards.data.local

import android.content.Context
import android.content.SharedPreferences
import com.eventcountdown.cards.domain.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

class UnsupportedSchema : Exception()

data class StoredCollection(val seeded: Boolean, val events: List<Event>, val draft: EventDraft?, val settings: AppSettings, val fallback: Boolean)

class CountdownPreferences(private val preferences: SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences("event_countdown_cards_v1", Context.MODE_PRIVATE))
    private var fallback = false

    fun read(): StoredCollection {
        val schema = preferences.getInt("schema_version", 1)
        if (schema > 1) throw UnsupportedSchema()
        require(schema == 1)
        fallback = false
        val seeded = preferences.getBoolean("sample_seeded", false)
        val events = JSONArray(preferences.getString("events_json", "[]"))
        val decoded = (0 until events.length()).map { decodeEvent(events.getJSONObject(it)) }
        require(decoded.map { it.id }.distinct().size == decoded.size)
        val draft = preferences.getString("draft_json", null)?.let { decodeDraft(JSONObject(it)) }
        val settings = preferences.getString("settings_json", null)?.let {
            val json = JSONObject(it)
            AppSettings(CardStyle.valueOf(json.getString("style")), json.getBoolean("animations"), json.getBoolean("haptics"))
        } ?: AppSettings()
        require(seeded || (decoded.isEmpty() && draft == null))
        return StoredCollection(seeded, decoded, draft, settings, fallback)
    }

    fun write(events: List<Event>, draft: EventDraft?, settings: AppSettings): Boolean {
        val before = preferences.all.toMap()
        val array = JSONArray()
        events.forEach { array.put(encodeEvent(it)) }
        val editor = preferences.edit().putInt("schema_version", 1).putBoolean("sample_seeded", true)
            .putString("events_json", array.toString())
            .putString("settings_json", JSONObject().put("style", settings.cardStyle.name).put("animations", settings.gentleAnimationsEnabled).put("haptics", settings.hapticsEnabled).toString())
        if (draft == null) editor.remove("draft_json") else editor.putString("draft_json", encodeDraft(draft).toString())
        if (editor.commit()) return true
        val rollback = preferences.edit().clear()
        before.forEach { (key, value) ->
            when (value) {
                is String -> rollback.putString(key, value)
                is Int -> rollback.putInt(key, value)
                is Long -> rollback.putLong(key, value)
                is Float -> rollback.putFloat(key, value)
                is Boolean -> rollback.putBoolean(key, value)
                is Set<*> -> rollback.putStringSet(key, value.filterIsInstance<String>().toSet())
            }
        }
        rollback.commit()
        return false
    }

    private fun validId(value: String): String { UUID.fromString(value); return value }
    private fun picture(key: String): Picture = Picture.entries.find { it.name == key } ?: Picture.Birthday.also { fallback = true }
    private fun validName(value: String): String { require(value.isNotBlank() && value.codePointCount(0, value.length) <= 60); return value }
    private fun validNotes(value: String): String { require(value.codePointCount(0, value.length) <= 280); return value }
    private fun decodeEvent(j: JSONObject): Event = Event(
        validId(j.getString("id")), validName(j.getString("name")), Category.valueOf(j.getString("category")),
        LocalDate.parse(j.getString("date")), picture(j.getString("picture")), validNotes(j.getString("notes")),
        j.getBoolean("pin"), j.getLong("created"), j.getLong("updated"), if (j.isNull("archived")) null else j.getLong("archived")
    )
    private fun encodeEvent(e: Event): JSONObject = JSONObject().put("id", e.id).put("name", e.name).put("category", e.category.name)
        .put("date", e.eventDate.toString()).put("picture", e.picture.name).put("notes", e.notes).put("pin", e.pinnedInApp)
        .put("created", e.createdAt).put("updated", e.updatedAt).put("archived", e.manuallyArchivedAt ?: JSONObject.NULL)
    private fun decodeDraft(j: JSONObject): EventDraft {
        val mode = DraftMode.valueOf(j.getString("mode"))
        val source = if (j.isNull("source")) null else validId(j.getString("source"))
        require((mode == DraftMode.Edit) == (source != null))
        val name = j.getString("name")
        require(name.codePointCount(0, name.length) <= 60)
        return EventDraft(validId(j.getString("id")), mode, source, name, Category.valueOf(j.getString("category")),
            LocalDate.parse(j.getString("date")), picture(j.getString("picture")), validNotes(j.getString("notes")), j.getBoolean("pin"), j.getLong("updated"))
    }
    private fun encodeDraft(d: EventDraft): JSONObject = JSONObject().put("id", d.draftId).put("mode", d.mode.name)
        .put("source", d.sourceEventId ?: JSONObject.NULL).put("name", d.name).put("category", d.category.name)
        .put("date", d.eventDate.toString()).put("picture", d.picture.name).put("notes", d.notes).put("pin", d.pinnedInApp).put("updated", d.updatedAt)
}
