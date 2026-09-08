package config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class LintConfigLoaderTest {
    @Test
    fun `load default when file does not exist`() {
        val config = LintConfigLoader.loadFromJson("/nonexistent/path/rules.json")

        // Valores por defecto
        assertEquals(IdentifierFormat.CAMEL_CASE, config.identifierFormat)
        assertFalse(config.printlnArgumentCheck)
    }

    @Test
    fun `load from test resources json file`() {
        val resourcePath = "src/test/resources/test-lint-rules.json"
        val config = LintConfigLoader.loadFromJson(resourcePath)

        assertEquals(IdentifierFormat.SNAKE_CASE, config.identifierFormat)
        assertTrue(config.printlnArgumentCheck)
    }

    @Test
    fun `load custom config from dynamic json file`() {
        val tempFile = File.createTempFile("custom-rules", ".json")
        tempFile.writeText(
            """
            {
              "identifierFormat": "CAMEL_CASE",
              "printlnArgumentCheck": true
            }
            """.trimIndent(),
        )

        val config = LintConfigLoader.loadFromJson(tempFile.absolutePath)

        assertEquals(IdentifierFormat.CAMEL_CASE, config.identifierFormat)
        assertTrue(config.printlnArgumentCheck)

        tempFile.delete()
    }
}
