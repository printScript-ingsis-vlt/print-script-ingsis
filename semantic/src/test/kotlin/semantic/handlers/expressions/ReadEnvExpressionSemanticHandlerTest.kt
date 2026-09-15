package semantic.handlers.expressions

import ast.Identifier
import ast.NumberLiteral
import ast.ReadEnvExpression
import ast.StringLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import semantic.SemanticContext
import semantic.expressions.ExpressionSemanticAnalyzer
import semantic.handlers.expressions.read.ReadEnvExpressionSemanticHandler
import semantic.pos
import semantic.symbols.SemanticSymbol

class ReadEnvExpressionSemanticHandlerTest {
    @Test
    fun `readEnv returns the type expected by its context`() {
        val analysis =
            analyzer().analyze(
                ReadEnvExpression(StringLiteral("PORT", pos()), pos()),
                SemanticContext(),
                expectedType = "number",
            )

        assertEquals("number", analysis.type)
        assertTrue(analysis.errors.isEmpty())
    }

    @Test
    fun `readEnv accepts a string identifier as its environment variable name`() {
        val context = SemanticContext()
        context.symbols.declare("variableName", SemanticSymbol("string", initialized = true))

        val analysis =
            analyzer().analyze(
                ReadEnvExpression(Identifier("variableName", pos()), pos()),
                context,
                expectedType = "boolean",
            )

        assertEquals("boolean", analysis.type)
        assertTrue(analysis.errors.isEmpty())
    }

    @Test
    fun `readEnv rejects a non string environment variable name`() {
        val analysis =
            analyzer().analyze(
                ReadEnvExpression(NumberLiteral(8080.0, pos()), pos()),
                SemanticContext(),
                expectedType = "string",
            )

        assertEquals("string", analysis.type)
        assertEquals(
            "readEnv environment variable name must be a string",
            analysis.errors.single().message,
        )
    }

    @Test
    fun `readEnv requires a supported contextual type`() {
        val withoutContext =
            analyzer().analyze(
                ReadEnvExpression(StringLiteral("PORT", pos()), pos()),
                SemanticContext(),
            )
        val unsupportedContext =
            analyzer().analyze(
                ReadEnvExpression(StringLiteral("PORT", pos()), pos()),
                SemanticContext(),
                expectedType = "unknown",
            )

        assertNull(withoutContext.type)
        assertEquals(
            "readEnv must be used where string, number, or boolean is expected",
            withoutContext.errors.single().message,
        )
        assertNull(unsupportedContext.type)
        assertEquals(
            "readEnv cannot produce values of type 'unknown'",
            unsupportedContext.errors.single().message,
        )
    }

    private fun analyzer(): ExpressionSemanticAnalyzer =
        ExpressionSemanticAnalyzer(
            listOf(
                ReadEnvExpressionSemanticHandler(setOf("string", "number", "boolean")),
                NumberLiteralSemanticHandler(),
                StringLiteralSemanticHandler(),
                IdentifierSemanticHandler(),
            ),
        )
}
