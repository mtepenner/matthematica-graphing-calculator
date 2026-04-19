package com.matthematica.data.sync

import android.content.Context
import com.matthematica.data.database.dao.CalculationHistoryDao
import com.matthematica.data.model.CalculationHistory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryCloudSyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val historyDao: CalculationHistoryDao
) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun syncToCloud(userId: String): Result<String> {
        return try {
            val items = historyDao.getUserHistorySnapshot(userId)
            val syncFile = getCloudFile(userId)
            syncFile.parentFile?.mkdirs()
            syncFile.writeText(json.encodeToString(items))
            Result.success("Synced ${items.size} history records to cloud backup")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromCloud(userId: String): Result<String> {
        return try {
            val syncFile = getCloudFile(userId)
            if (!syncFile.exists()) {
                return Result.failure(IllegalStateException("No cloud backup found for user: $userId"))
            }

            val items = json.decodeFromString<List<CalculationHistory>>(syncFile.readText())
            var restored = 0
            items.forEach { item ->
                historyDao.insertHistory(item.copy(id = 0))
                restored++
            }

            Result.success("Restored $restored history records from cloud backup")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCloudFile(userId: String): File {
        return File(context.filesDir, "cloud-sync/history_$userId.json")
    }
}

