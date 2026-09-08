package semantic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import recurses.*
import recurses.valuedataclass.NumberValue
import rules.DeclarationValidator

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
}
