package com.vitkkk.rvcmobile.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.vitkkk.rvcmobile.model.RvcModel
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipInputStream

class ModelImporter(
    private val context: Context,
    private val repository: ModelRepository
) {
    data class ImportResult(val model: RvcModel?, val message: String)

    fun importUri(uri: Uri): ImportResult {
        val displayName = queryName(uri) ?: "RVC-model"
        val ext = displayName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "zip" -> importZip(uri, displayName.substringBeforeLast('.'))
            "pth" -> importSingle(uri, displayName, true)
            "index" -> importSingle(uri, displayName, false)
            else -> ImportResult(null, "Formato não suportado: .$ext")
        }
    }

    private fun importSingle(uri: Uri, displayName: String, checkpoint: Boolean): ImportResult {
        val base = displayName.substringBeforeLast('.')
        val existing = repository.list().firstOrNull { it.name.equals(base, ignoreCase = true) }
        val id = existing?.id ?: UUID.randomUUID().toString()
        val dir = File(repository.modelsRoot, id).apply { mkdirs() }
        val destination = File(dir, sanitize(displayName))
        copyUri(uri, destination)

        val model = (existing ?: RvcModel(id, base, null, null)).copy(
            checkpointPath = if (checkpoint) destination.absolutePath else existing?.checkpointPath,
            indexPath = if (!checkpoint) destination.absolutePath else existing?.indexPath,
            sizeBytes = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        )
        repository.upsert(model)
        val state = when {
            model.checkpointPath != null && model.indexPath != null -> "PTH + INDEX importados"
            checkpoint -> "Checkpoint importado. Você pode adicionar o INDEX depois."
            else -> "INDEX importado. Adicione o PTH correspondente para usar o modelo."
        }
        return ImportResult(model, state)
    }

    private fun importZip(uri: Uri, fallbackName: String): ImportResult {
        val id = UUID.randomUUID().toString()
        val dir = File(repository.modelsRoot, id).apply { mkdirs() }
        var checkpoint: File? = null
        var index: File? = null

        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val leaf = File(entry.name).name
                        val ext = leaf.substringAfterLast('.', "").lowercase()
                        if (ext == "pth" || ext == "index") {
                            val target = File(dir, sanitize(leaf))
                            FileOutputStream(target).use { out -> zip.copyTo(out) }
                            if (ext == "pth" && checkpoint == null) checkpoint = target
                            if (ext == "index" && index == null) index = target
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: return ImportResult(null, "Não foi possível abrir o ZIP.")

        if (checkpoint == null && index == null) {
            dir.deleteRecursively()
            return ImportResult(null, "O ZIP não contém arquivos .pth ou .index reconhecíveis.")
        }

        val detectedName = checkpoint?.nameWithoutExtension
            ?: index?.nameWithoutExtension
            ?: fallbackName
        val model = RvcModel(
            id = id,
            name = detectedName,
            checkpointPath = checkpoint?.absolutePath,
            indexPath = index?.absolutePath,
            sizeBytes = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        )
        repository.upsert(model)
        return ImportResult(
            model,
            if (checkpoint != null && index != null) "Modelo e INDEX detectados automaticamente." else "Importação parcial concluída."
        )
    }

    private fun copyUri(uri: Uri, target: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(target).use { output -> input.copyTo(output) }
        } ?: error("Não foi possível abrir o arquivo selecionado")
    }

    private fun queryName(uri: Uri): String? {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getString(0)
        }
        return uri.lastPathSegment
    }

    private fun sanitize(name: String): String = name.replace(Regex("[^A-Za-z0-9._ -]"), "_")
}
