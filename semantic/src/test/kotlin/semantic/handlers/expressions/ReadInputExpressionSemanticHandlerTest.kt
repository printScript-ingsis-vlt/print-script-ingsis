package semantic.handlers.expressions

import ast.Identifier
import ast.NumberLiteral
import ast.ReadInputExpression
import ast.StringLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import semantic.SemanticContext
import semantic.expressions.ExpressionSemanticAnalyzer
import semantic.pos
import semantic.symbols.SemanticSymbol

class ReadInputExpressionSemanticHandlerTest {
    @Test
    fun `readInput returns the type expected by its context`() {
        val analysis =
            analyzer().analyze(
                ReadInputExpression(StringLiteral("Age", pos()), pos()),
                SemanticContext(),
                expectedType = "number",
            )

        assertEquals("number", analysis.type)
        assertTrue(analysis.errors.isEmpty())
    }

    @Test
    fun `readInput accepts a string identifier as its prompt`() {
        val context = SemanticContext()
        context.symbols.declare("prompt", SemanticSymbol("string", initialized = true))

        val analysis =
            analyzer().analyze(
                ReadInputExpression(Identifier("prompt", pos()), pos()),
                context,
                expectedType = "boolean",
            )

        assertEquals("boolean", analysis.type)
        assertTrue(analysis.errors.isEmpty())
    }

    @Test
    fun `readInput rejects a non string prompt`() {
        val analysis =
            analyzer().analyze(
                ReadInputExpression(NumberLiteral(1.0, pos()), pos()),
                SemanticContext(),
                expectedType = "string",
            )

        assertEquals("string", analysis.type)
        assertEquals("readInput prompt must be a string", analysis.errors.single().message)
    }

    @Test
    fun `readInput requires a supported contextual type`() {
        val withoutContext =
            analyzer().analyze(
                ReadInputExpression(StringLiteral("Value", pos()), pos()),
                SemanticContext(),
            )
        val unsupportedContext =
            analyzer().analyze(
                ReadInputExpression(StringLiteral("Value", pos()), pos()),
                SemanticContext(),
                expectedType = "unknown",
            )

        assertNull(withoutContext.type)
        assertEquals(
            "readInput must be used where string, number, or boolean is expected",
            withoutContext.errors.single().message,
        )
        assertNull(unsupportedContext.type)
        assertEquals(
            "readInput cannot produce values of type 'unknown'",
            unsupportedContext.errors.single().message,
        )
    }

    @Test
    fun `readInput supports types enabled by its configuration`() {
        val analysis =
            analyzer(setOf("string", "number", "boolean", "float")).analyze(
                ReadInputExpression(StringLiteral("Price", pos()), pos()),
                SemanticContext(),
                expectedType = "float",
            )

        assertEquals("float", analysis.type)
        assertTrue(analysis.errors.isEmpty())
    }

    private fun analyzer(
        supportedReturnTypes: Set<String> = setOf("string", "number", "boolean"),
    ): ExpressionSemanticAnalyzer =
        ExpressionSemanticAnalyzer(
            listOf(
                ReadInputExpressionSemanticHandler(supportedReturnTypes),
                NumberLiteralSemanticHandler(),
                StringLiteralSemanticHandler(),
                IdentifierSemanticHandler(),
            ),
        )
}
