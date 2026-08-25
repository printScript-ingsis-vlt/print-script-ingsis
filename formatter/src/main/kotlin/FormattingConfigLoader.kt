import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

object FormattingConfigLoader {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadFromJson(filePath: String): FormattingRules {
        val file = File(filePath)
        return if (file.exists()) {
            json.decodeFromString<FormattingRules>(file.readText())
        } else {
            FormattingRules.default()
        }
    }

    fun loadDefault(): FormattingRules = FormattingRules.default()
}
