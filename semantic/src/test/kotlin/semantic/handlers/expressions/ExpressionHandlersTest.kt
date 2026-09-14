package semantic.handlers.expressions

import ast.BinaryExpression
import ast.Identifier
import ast.NumberLiteral
import ast.StringLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import semantic.SemanticContext
import semantic.expressions.ExpressionSemanticAnalyzer
import semantic.pos
import semantic.symbols.SemanticSymbol

class ExpressionHandlersTest {
    @Test
    fun `analyzes number and string literals`() {
        val analyzer = createAnalyzer()
        val context = SemanticContext()

        val number = analyzer.analyze(NumberLiteral(42.0, pos()), context)
        val string = analyzer.analyze(StringLiteral("hello", pos()), context)

        assertEquals("number", number.type)
        assertEquals(42.0, number.knownNumberValue)
        assertTrue(number.errors.isEmpty())
        assertEquals("string", string.type)
        assertEquals(null, string.knownNumberValue)
        assertTrue(string.errors.isEmpty())
    }

    @Test
    fun `analyzes declared identifiers and preserves their known number value`() {
        val context = SemanticContext()
        context.symbols.declare("count", SemanticSymbol("number", initialized = true, knownNumberValue = 7.0))

        val analysis = createAnalyzer().analyze(Identifier("count", pos()), context)

        assertEquals("number", analysis.type)
        assertEquals(7.0, analysis.knownNumberValue)
        assertTrue(analysis.errors.isEmpty())
    }

    @Test
    fun `reports undeclared and uninitialized identifiers`() {
        val analyzer = createAnalyzer()
        val context = SemanticContext()

        val undeclared = analyzer.analyze(Identifier("missing", pos()), context)
        context.symbols.declare("pending", SemanticSymbol("number", initialized = false))
        val uninitialized = analyzer.analyze(Identifier("pending", pos()), context)

        assertEquals("Variable 'missing' is not declared", undeclared.errors.single().message)
        assertEquals("number", uninitialized.type)
        assertEquals("Variable 'pending' is not initialized", uninitialized.errors.single().message)
    }

    @Test
    fun `infers numeric binary expressions and evaluates known values`() {
        val expression =
            BinaryExpression(
                NumberLiteral(2.0, pos()),
                "*",
                NumberLiteral(3.0, pos()),
                pos(),
            )

        val analysis = createAnalyzer().analyze(expression, SemanticContext())

        assertEquals("number", analysis.type)
        assertEquals(6.0, analysis.knownNumberValue)
        assertTrue(analysis.errors.isEmpty())
    }

    @Test
    fun `allows string concatenation and rejects other string operations`() {
        val concatenation =
            BinaryExpression(
                StringLiteral("hello", pos()),
                "+",
                NumberLiteral(1.0, pos()),
                pos(),
            )
        val subtraction =
            BinaryExpression(
                StringLiteral("hello", pos()),
                "-",
                NumberLiteral(1.0, pos()),
                pos(),
            )
        val analyzer = createAnalyzer()

        val concatenationAnalysis = analyzer.analyze(concatenation, SemanticContext())
        val subtractionAnalysis = analyzer.analyze(subtraction, SemanticContext())

        assertEquals("string", concatenationAnalysis.type)
        assertTrue(concatenationAnalysis.errors.isEmpty())
        assertEquals("Operator '-' requires operand", subtractionAnalysis.errors.single().message)
    }

    @Test
    fun `reports division by zero and unknown operators`() {
        val division =
            BinaryExpression(
                NumberLiteral(10.0, pos()),
                "/",
                NumberLiteral(0.0, pos()),
                pos(),
            )
        val unknown =
            BinaryExpression(
                NumberLiteral(1.0, pos()),
                "%",
                NumberLiteral(1.0, pos()),
                pos(),
            )
        val analyzer = createAnalyzer()

        val divisionAnalysis = analyzer.analyze(division, SemanticContext())
        val unknownAnalysis = analyzer.analyze(unknown, SemanticContext())

        assertEquals("Division by zero", divisionAnalysis.errors.single().message)
        assertEquals("Unknown operator '%'", unknownAnalysis.errors.single().message)
    }

    @Test
    fun `reports division by a computed or referenced zero value`() {
        val computedZero =
            BinaryExpression(
                NumberLiteral(5.0, pos()),
                "-",
                NumberLiteral(5.0, pos()),
                pos(),
            )
        val division = BinaryExpression(NumberLiteral(10.0, pos()), "/", computedZero, pos())
        val context = SemanticContext()
        context.symbols.declare("zero", SemanticSymbol("number", initialized = true, knownNumberValue = 0.0))
        val referencedDivision =
            BinaryExpression(
                NumberLiteral(1.0, pos()),
                "/",
                Identifier("zero", pos()),
                pos(),
            )
        val analyzer = createAnalyzer()

        assertEquals(
            "Division by zero",
            analyzer.analyze(division, context).errors.single().message,
        )
        assertEquals(
            "Division by zero",
            analyzer.analyze(referencedDivision, context).errors.single().message,
        )
    }

    private fun createAnalyzer(): ExpressionSemanticAnalyzer =
        ExpressionSemanticAnalyzer(
            listOf(
                NumberLiteralSemanticHandler(),
                StringLiteralSemanticHandler(),
                IdentifierSemanticHandler(),
                BinaryExpressionSemanticHandler(),
            ),
        )
}
