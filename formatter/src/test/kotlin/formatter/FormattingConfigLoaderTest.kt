package formatter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File

class FormattingConfigLoaderTest {
    @Test
    fun `load default when file does not exist`() {
        // default = nada forzado, todo null
        val rules = FormattingConfigLoader.loadFromJson("/nonexistent/path.json")
        assertNull(rules.spaceBeforeColon)
        assertNull(rules.spaceAfterColon)
        assertNull(rules.spaceAroundEqual)
        assertNull(rules.newlinesBeforePrintln)
    }

    @Test
    fun `load default rules`() {
        val rules = FormattingConfigLoader.loadDefault()
        assertNull(rules.spaceBeforeColon)
        assertNull(rules.spaceAfterColon)
        assertNull(rules.spaceAroundEqual)
        assertNull(rules.newlinesBeforePrintln)
    }

    @Test
    fun `load from json file`() {
        val jsonContent = """{
  "spaceBeforeColon": false,
  "spaceAfterColon": true,
  "spaceAroundEqual": false,
  "newlinesBeforePrintln": 2
}"""
        val tempFile = File.createTempFile("test-rules", ".json")
        tempFile.writeText(jsonContent)

        val rules = FormattingConfigLoader.loadFromJson(tempFile.absolutePath)
        assertEquals(false, rules.spaceBeforeColon)
        assertEquals(true, rules.spaceAfterColon)
        assertEquals(false, rules.spaceAroundEqual)
        assertEquals(2, rules.newlinesBeforePrintln)

        tempFile.delete()
    }
}
