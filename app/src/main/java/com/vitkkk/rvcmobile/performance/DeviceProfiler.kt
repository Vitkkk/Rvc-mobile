package com.vitkkk.rvcmobile.performance

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs

data class DeviceProfile(
    val manufacturer: String,
    val model: String,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val availableStorageBytes: Long,
    val hasVulkan: Boolean,
    val androidVersion: String,
    val recommendedProfile: PerformanceProfile,
    val recommendedBatchSize: Int
)

enum class PerformanceProfile { ECONOMY, BALANCED, PERFORMANCE }

object DeviceProfiler {
    fun inspect(context: Context): DeviceProfile {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        val hasVulkan = context.packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL)
        val totalGb = memory.totalMem / (1024.0 * 1024.0 * 1024.0)

        val profile = when {
            totalGb >= 12 && hasVulkan -> PerformanceProfile.PERFORMANCE
            totalGb >= 6 -> PerformanceProfile.BALANCED
            else -> PerformanceProfile.ECONOMY
        }
        val batch = when {
            totalGb >= 16 -> 4
            totalGb >= 10 -> 3
            totalGb >= 6 -> 2
            else -> 1
        }

        return DeviceProfile(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            totalRamBytes = memory.totalMem,
            availableRamBytes = memory.availMem,
            availableStorageBytes = stat.availableBytes,
            hasVulkan = hasVulkan,
            androidVersion = Build.VERSION.RELEASE,
            recommendedProfile = profile,
            recommendedBatchSize = batch
        )
    }
}
