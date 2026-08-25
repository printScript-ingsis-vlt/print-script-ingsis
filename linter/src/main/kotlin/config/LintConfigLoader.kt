package config

import java.io.File
import kotlinx.serialization.json.Json

object LintConfigLoader {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadFromJson(filePath: String) : LintConfig { // --> Convierte el JSON a LintConfig
        val file = File(filePath)
        return if (file.exists()) {
            json.decodeFromString<LintConfig>(file.readText())
        } else {
            LintConfig()
        }
    }
}
