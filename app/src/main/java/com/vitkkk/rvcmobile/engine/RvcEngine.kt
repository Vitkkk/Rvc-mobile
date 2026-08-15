package com.vitkkk.rvcmobile.engine

import java.io.File

enum class F0Method { RMVPE, FCPE, HARVEST, CREPE, DIO }

data class EngineCapabilities(
    val name: String,
    val supportsV1: Boolean,
    val supportsV2: Boolean,
    val supportedF0: Set<F0Method>,
    val supportsIndex: Boolean,
    val supportsGpu: Boolean
)

data class ConversionRequest(
    val checkpoint: File,
    val index: File?,
    val inputAudio: File,
    val outputAudio: File,
    val pitchSemitones: Int = 0,
    val f0Method: F0Method = F0Method.RMVPE,
    val indexRate: Float = 0.75f,
    val filterRadius: Int = 3,
    val rmsMixRate: Float = 0.25f,
    val protect: Float = 0.33f
)

data class ConversionProgress(
    val fraction: Float,
    val stage: String
)

interface RvcEngine {
    val capabilities: EngineCapabilities
    suspend fun convert(request: ConversionRequest, onProgress: (ConversionProgress) -> Unit): Result<File>
    suspend fun warmUp(): Result<Unit>
    fun release()
}

/**
 * Runtime intentionally reports unavailable until the Android-native inference graph is wired.
 * Keeping this boundary separate prevents UI/storage code from depending on desktop Python.
 */
class UnavailableRvcEngine : RvcEngine {
    override val capabilities = EngineCapabilities(
        name = "Runtime não instalado",
        supportsV1 = false,
        supportsV2 = false,
        supportedF0 = emptySet(),
        supportsIndex = false,
        supportsGpu = false
    )

    override suspend fun convert(
        request: ConversionRequest,
        onProgress: (ConversionProgress) -> Unit
    ): Result<File> = Result.failure(
        IllegalStateException("O backend RVC Android ainda não foi instalado.")
    )

    override suspend fun warmUp(): Result<Unit> = Result.failure(
        IllegalStateException("Runtime indisponível")
    )

    override fun release() = Unit
}
