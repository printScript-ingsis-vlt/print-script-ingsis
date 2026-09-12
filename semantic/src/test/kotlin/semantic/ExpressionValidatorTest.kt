package semantic

import ast.BinaryExpression
import ast.Identifier
import ast.NumberLiteral
import ast.PrintStatement
import ast.StringLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import runtime.Environment
import runtime.Variable
import runtime.valuedataclass.NumberValue
import semantic.rules.ExpressionValidator

class ExpressionValidatorTest {
    private val validator = ExpressionValidator()
    private val env = Environment()

    @Test
    fun `valid arithmetic expression produces no errors`() {
        val expr = BinaryExpression(NumberLiteral(5.0, pos()), "+", NumberLiteral(3.0, pos()), pos())
        val stmt = PrintStatement(expr, pos())
        val errors = validator.check(stmt, env)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `subtracting strings produces error`() {
        val expr = BinaryExpression(StringLiteral("hello", pos()), "-", NumberLiteral(2.0, pos()), pos())
        val stmt = PrintStatement(expr, pos())
        val errors = validator.check(stmt, env)
        assertEquals(1, errors.size)
        assertTrue(errors.first().message.contains("Operator '-' requires"))
    }

    @Test
    fun `direct division by zero produces error`() {
        val expr = BinaryExpression(NumberLiteral(10.0, pos()), "/", NumberLiteral(0.0, pos()), pos())
        val stmt = PrintStatement(expr, pos())
        val errors = validator.check(stmt, env)
        assertEquals(1, errors.size)
        assertEquals("Division by zero", errors.first().message)
    }

    @Test
    fun `composed constant division by zero produces error`() {
        // 10 / (5 - 5) por ejemplo deberia ser invalido
        val denom = BinaryExpression(NumberLiteral(5.0, pos()), "-", NumberLiteral(5.0, pos()), pos())
        val expr = BinaryExpression(NumberLiteral(10.0, pos()), "/", denom, pos())
        val stmt = PrintStatement(expr, pos())
        val errors = validator.check(stmt, env)
        assertEquals(1, errors.size)
        assertEquals("Division by zero", errors.first().message)
    }

    @Test
    fun `unknown operators produce an error`() {
        val expr = BinaryExpression(NumberLiteral(1.0, pos()), "%", NumberLiteral(1.0, pos()), pos())

        val errors = validator.check(PrintStatement(expr, pos()), env)

        assertEquals("Unknown operator '%'", errors.single().message)
    }

    @Test
    fun `division by an initialized variable with zero value produces an error`() {
        env.declare("zero", Variable("number", NumberValue(0.0)))
        val expr = BinaryExpression(NumberLiteral(1.0, pos()), "/", Identifier("zero", pos()), pos())

        val errors = validator.check(PrintStatement(expr, pos()), env)

        assertEquals("Division by zero", errors.single().message)
    }

    @Test
    fun `adding strings is valid`() {
        val expr = BinaryExpression(StringLiteral("hello", pos()), "+", StringLiteral(" world", pos()), pos())

        assertTrue(validator.check(PrintStatement(expr, pos()), env).isEmpty())
    }
}
