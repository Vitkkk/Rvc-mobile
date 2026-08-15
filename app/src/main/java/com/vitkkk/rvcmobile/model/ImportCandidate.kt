package com.vitkkk.rvcmobile.model

data class ImportCandidate(
    val displayName: String,
    val checkpointName: String? = null,
    val indexName: String? = null,
    val sourceType: String
)

object SupportedModelFiles {
    fun classify(fileName: String): String? {
        val lower = fileName.lowercase()
        return when {
            lower.endsWith(".pth") -> "RVC_CHECKPOINT"
            lower.endsWith(".index") -> "RVC_INDEX"
            lower.endsWith(".zip") -> "RVC_PACKAGE"
            else -> null
        }
    }
}
