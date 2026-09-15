package semantic.handlers.expressions

import ast.BooleanLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import semantic.SemanticContext
import semantic.expressions.ExpressionSemanticAnalyzer
import semantic.pos

class BooleanLiteralSemanticHandlerTest {
    @Test
    fun `infers true as boolean`() {
        val analysis = analyze(true)

        assertEquals("boolean", analysis.type)
        assertTrue(analysis.errors.isEmpty())
    }

    @Test
    fun `infers false as boolean`() {
        val analysis = analyze(false)

        assertEquals("boolean", analysis.type)
        assertTrue(analysis.errors.isEmpty())
    }

    private fun analyze(value: Boolean) =
        ExpressionSemanticAnalyzer(listOf(BooleanLiteralSemanticHandler()))
            .analyze(BooleanLiteral(value, pos()), SemanticContext())
}
