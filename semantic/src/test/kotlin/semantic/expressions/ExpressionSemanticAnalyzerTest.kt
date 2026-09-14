package semantic.expressions

import ast.BinaryExpression
import ast.Expr
import ast.NumberLiteral
import ast.StringLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import semantic.SemanticContext

class ExpressionSemanticAnalyzerTest {
    private val context = SemanticContext()

    @Test
    fun `delegates an expression to its matching handler`() {
        val analyzer = ExpressionSemanticAnalyzer(listOf(NumberLiteralHandler()))

        val analysis = analyzer.analyze(NumberLiteral(42.0, pos()), context)

        assertEquals("number", analysis.type)
        assertEquals(42.0, analysis.knownNumberValue)
        assertEquals(emptyList<Any>(), analysis.errors)
    }

    @Test
    fun `allows a handler to analyze child expressions recursively`() {
        val analyzer =
            ExpressionSemanticAnalyzer(
                listOf(
                    BinaryExpressionHandler(),
                    NumberLiteralHandler(),
                ),
            )
        val expression =
            BinaryExpression(
                NumberLiteral(2.0, pos()),
                "+",
                NumberLiteral(3.0, pos()),
                pos(),
            )

        val analysis = analyzer.analyze(expression, context)

        assertEquals("number", analysis.type)
        assertEquals(5.0, analysis.knownNumberValue)
        assertEquals(emptyList<Any>(), analysis.errors)
    }

    @Test
    fun `fails when no handler supports an expression`() {
        val analyzer = ExpressionSemanticAnalyzer(listOf(NumberLiteralHandler()))

        val exception =
            assertThrows(IllegalStateException::class.java) {
                analyzer.analyze(StringLiteral("text", pos()), context)
            }

        assertEquals("No semantic expression handler found for: StringLiteral", exception.message)
    }

    @Test
    fun `fails when multiple handlers support an expression`() {
        val analyzer =
            ExpressionSemanticAnalyzer(
                listOf(
                    NumberLiteralHandler(),
                    NumberLiteralHandler(),
                ),
            )

        val exception =
            assertThrows(IllegalStateException::class.java) {
                analyzer.analyze(NumberLiteral(1.0, pos()), context)
            }

        assertEquals(
            "Ambiguous semantic expression handlers for NumberLiteral: " +
                "NumberLiteralHandler, NumberLiteralHandler",
            exception.message,
        )
    }

    private class NumberLiteralHandler : ExpressionSemanticHandler {
        override fun canHandle(expression: Expr): Boolean = expression is NumberLiteral

        override fun analyze(
            expression: Expr,
            context: SemanticContext,
            analyzeChild: (Expr) -> ExpressionAnalysis,
        ): ExpressionAnalysis {
            val literal = expression as NumberLiteral

            return ExpressionAnalysis(
                type = "number",
                errors = emptyList(),
                knownNumberValue = literal.value,
            )
        }
    }

    private class BinaryExpressionHandler : ExpressionSemanticHandler {
        override fun canHandle(expression: Expr): Boolean = expression is BinaryExpression

        override fun analyze(
            expression: Expr,
            context: SemanticContext,
            analyzeChild: (Expr) -> ExpressionAnalysis,
        ): ExpressionAnalysis {
            val binary = expression as BinaryExpression
            val left = analyzeChild(binary.left)
            val right = analyzeChild(binary.right)
            val knownNumberValue =
                if (
                    binary.operator == "+" &&
                    left.knownNumberValue != null &&
                    right.knownNumberValue != null
                ) {
                    left.knownNumberValue + right.knownNumberValue
                } else {
                    null
                }

            return ExpressionAnalysis(
                type = if (left.type == "number" && right.type == "number") "number" else null,
                errors = left.errors + right.errors,
                knownNumberValue = knownNumberValue,
            )
        }
    }
}
