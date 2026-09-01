package semantic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import recurses.*
import rules.ExpressionValidator

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
}
