package com.matthematica.domain.calculator

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraphPresetService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun savePreset(preset: SavedGraphPreset): Result<String> {
        return try {
            val presets = loadPresets().getOrDefault(emptyList()).toMutableList()
            presets.removeAll { it.name.equals(preset.name, ignoreCase = true) }
            presets.add(preset)
            presetFile().parentFile?.mkdirs()
            presetFile().writeText(json.encodeToString(presets))
            Result.success("Saved preset '${preset.name}'")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loadPresets(): Result<List<SavedGraphPreset>> {
        return try {
            val file = presetFile()
            if (!file.exists()) return Result.success(emptyList())
            val items = json.decodeFromString<List<SavedGraphPreset>>(file.readText())
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun presetFile(): File = File(context.filesDir, "graphing/presets.json")
}

@kotlinx.serialization.Serializable
data class SavedGraphPreset(
    val name: String,
    val expression: String,
    val xMin: Double,
    val xMax: Double
)

