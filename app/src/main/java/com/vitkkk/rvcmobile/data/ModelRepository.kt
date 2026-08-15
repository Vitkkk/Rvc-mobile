package com.vitkkk.rvcmobile.data

import android.content.Context
import com.vitkkk.rvcmobile.model.RvcModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ModelRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("rvc_models", Context.MODE_PRIVATE)
    val modelsRoot: File = File(context.filesDir, "Models").apply { mkdirs() }

    fun list(): List<RvcModel> {
        val raw = prefs.getString(KEY_MODELS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(
                        RvcModel(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            checkpointPath = item.optString("checkpointPath").takeIf { it.isNotBlank() },
                            indexPath = item.optString("indexPath").takeIf { it.isNotBlank() },
                            version = item.optString("version", "Unknown"),
                            sampleRate = if (item.has("sampleRate") && !item.isNull("sampleRate")) item.getInt("sampleRate") else null,
                            hasF0 = if (item.has("hasF0") && !item.isNull("hasF0")) item.getBoolean("hasF0") else null,
                            sizeBytes = item.optLong("sizeBytes", 0L)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun upsert(model: RvcModel) {
        val items = list().toMutableList()
        val index = items.indexOfFirst { it.id == model.id }
        if (index >= 0) items[index] = model else items.add(model)
        save(items)
    }

    fun rename(id: String, newName: String) {
        val items = list().map { if (it.id == id) it.copy(name = newName.trim()) else it }
        save(items)
    }

    fun delete(id: String) {
        val model = list().firstOrNull { it.id == id }
        model?.checkpointPath?.let { runCatching { File(it).delete() } }
        model?.indexPath?.let { runCatching { File(it).delete() } }
        File(modelsRoot, id).deleteRecursively()
        save(list().filterNot { it.id == id })
    }

    fun totalSizeBytes(): Long = list().sumOf { it.sizeBytes }

    private fun save(models: List<RvcModel>) {
        val array = JSONArray()
        models.forEach { model ->
            array.put(JSONObject().apply {
                put("id", model.id)
                put("name", model.name)
                put("checkpointPath", model.checkpointPath ?: "")
                put("indexPath", model.indexPath ?: "")
                put("version", model.version)
                put("sampleRate", model.sampleRate ?: JSONObject.NULL)
                put("hasF0", model.hasF0 ?: JSONObject.NULL)
                put("sizeBytes", model.sizeBytes)
            })
        }
        prefs.edit().putString(KEY_MODELS, array.toString()).apply()
    }

    private companion object {
        const val KEY_MODELS = "models"
    }
}
