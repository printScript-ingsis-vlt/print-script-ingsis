package formatter

import ast.BooleanLiteral
import ast.IfStatement
import ast.Position
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FormatterConfigurationsTest {
    private val position = Position(1, 1)

    @Test
    fun `version 1_1 adds its statement and expression handlers`() {
        val ifStatement = IfStatement(BooleanLiteral(true, position), emptyList(), null, position)
        val booleanLiteral = BooleanLiteral(true, position)

        assertFalse(FormatterConfigurations.v1_0.statementHandlers.any { it.canHandle(ifStatement) })
        assertFalse(FormatterConfigurations.v1_0.expressionHandlers.any { it.canHandle(booleanLiteral) })
        assertTrue(FormatterConfigurations.v1_1.statementHandlers.any { it.canHandle(ifStatement) })
        assertTrue(FormatterConfigurations.v1_1.expressionHandlers.any { it.canHandle(booleanLiteral) })
    }
}
