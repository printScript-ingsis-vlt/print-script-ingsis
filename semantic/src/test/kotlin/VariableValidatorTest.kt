package semantic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import recurses.*
import recurses.valuedataclass.NumberValue
import rules.VariableValidator

class VariableValidatorTest {

    private val validator = VariableValidator()

    @Test
    fun `using undeclared variable produces error`() {
        val env = Environment()
        val stmt = VariableDeclaration("x", "number", Identifier("y", pos()), pos())
        val errors = validator.check(stmt, env)
        assertEquals(1, errors.size)
        assertEquals("Variable 'y' is not declared", errors.first().message)
    }

    @Test
    fun `using uninitialized variable produces error`() {
        val env = Environment()
        env.declare("y", Variable("number", null)) // declarada pero sin valor
        val stmt = PrintStatement(Identifier("y", pos()), pos())
        val errors = validator.check(stmt, env)
        assertEquals(1, errors.size)
        assertEquals("Variable 'y' is not initialized", errors.first().message)
    }

    @Test
    fun `using initialized variable produces no error`() {
        val env = Environment()
        env.declare("y", Variable("number", NumberValue(5.0)))
        val stmt = PrintStatement(Identifier("y", pos()), pos())
        val errors = validator.check(stmt, env)
        assertTrue(errors.isEmpty())
    }
}
