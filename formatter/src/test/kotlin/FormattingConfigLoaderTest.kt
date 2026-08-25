import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class FormattingConfigLoaderTest {
    @Test
    fun `load default when file does not exist`() {
        val rules = FormattingConfigLoader.loadFromJson("/nonexistent/path.json")
        assertEquals(true, rules.spaceBeforeColon)
        assertEquals(true, rules.spaceAfterColon)
        assertEquals(true, rules.spaceAroundEqual)
        assertEquals(1, rules.newlinesBeforePrintln)
    }

    @Test
    fun `load default rules`() {
        val rules = FormattingConfigLoader.loadDefault()
        assertEquals(true, rules.spaceBeforeColon)
        assertEquals(true, rules.spaceAfterColon)
        assertEquals(true, rules.spaceAroundEqual)
        assertEquals(1, rules.newlinesBeforePrintln)
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
