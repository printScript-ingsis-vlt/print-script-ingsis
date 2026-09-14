package parser.grammar

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GrammarConfigurationTest {
    @Test
    fun `no permite una lista de reglas vacia`() {
        assertThrows(IllegalArgumentException::class.java) {
            GrammarConfiguration(statementRules = emptyList())
        }
    }
}
