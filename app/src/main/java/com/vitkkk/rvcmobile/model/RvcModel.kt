package com.vitkkk.rvcmobile.model

data class RvcModel(
    val id: String,
    val name: String,
    val checkpointPath: String?,
    val indexPath: String?,
    val version: String = "Unknown",
    val sampleRate: Int? = null,
    val hasF0: Boolean? = null,
    val sizeBytes: Long = 0L
)
