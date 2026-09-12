package semantic

import ast.Assignment
import ast.BinaryExpression
import ast.Identifier
import ast.NumberLiteral
import ast.StringLiteral
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import runtime.Environment
import runtime.Variable
import runtime.valuedataclass.NumberValue
import semantic.rules.DeclarationValidator

class DeclarationValidatorTest {
    private val validator = DeclarationValidator()
    private val env = Environment()

    @Test
    fun `valid number declaration produces no error`() {
        val stmt = VariableDeclaration("x", "number", NumberLiteral(10.0, pos()), pos())
        val errors = validator.check(stmt, env)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `invalid type produces error`() {
        val stmt = VariableDeclaration("x", "boolean", null, pos())
        val errors = validator.check(stmt, env)
        assertEquals(1, errors.size)
        assertEquals("Invalid type 'boolean'", errors.first().message)
    }

    @Test
    fun `type mismatch in declaration produces error`() {
        val stmt = VariableDeclaration("x", "number", StringLiteral("hello", pos()), pos())
        val errors = validator.check(stmt, env)
        assertEquals(1, errors.size)
        assertEquals("Cannot assign string to number", errors.first().message)
    }

    @Test
    fun `invalid assignment type produces error`() {
        env.declare("x", Variable("number", NumberValue(5.0)))
        val stmt = Assignment("x", StringLiteral("text", pos()), pos())
        val errors = validator.check(stmt, env)
        assertEquals(1, errors.size)
        assertEquals("Cannot assign string to number", errors.first().message)
    }

    @Test
    fun `string concatenation can initialize a string variable`() {
        val value = BinaryExpression(StringLiteral("hello", pos()), "+", NumberLiteral(1.0, pos()), pos())
        val stmt = VariableDeclaration("message", "string", value, pos())

        assertTrue(validator.check(stmt, env).isEmpty())
    }

    @Test
    fun `identifier type is used when validating declarations`() {
        env.declare("message", Variable("string", NumberValue(1.0)))
        val stmt = VariableDeclaration("count", "number", Identifier("message", pos()), pos())

        val errors = validator.check(stmt, env)

        assertEquals("Cannot assign string to number", errors.single().message)
    }

    @Test
    fun `assignment to an undeclared variable is ignored by declaration validation`() {
        val stmt = Assignment("unknown", NumberLiteral(1.0, pos()), pos())

        assertTrue(validator.check(stmt, env).isEmpty())
    }
}
