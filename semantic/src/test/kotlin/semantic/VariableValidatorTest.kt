package semantic

import ast.Assignment
import ast.BinaryExpression
import ast.Identifier
import ast.PrintStatement
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import runtime.Environment
import runtime.Variable
import runtime.valuedataclass.NumberValue
import semantic.rules.VariableValidator

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

    @Test
    fun `assignment reports an undeclared target and referenced variable`() {
        val env = Environment()
        val stmt = Assignment("target", Identifier("value", pos()), pos())

        val errors = validator.check(stmt, env)

        assertEquals(2, errors.size)
        assertEquals("Variable 'target' is not declared", errors[0].message)
        assertEquals("Variable 'value' is not declared", errors[1].message)
    }

    @Test
    fun `binary expressions validate both operands`() {
        val env = Environment()
        env.declare("initialized", Variable("number", NumberValue(1.0)))
        env.declare("uninitialized", Variable("number", null))
        val expression =
            BinaryExpression(
                Identifier("initialized", pos()),
                "+",
                Identifier("uninitialized", pos()),
                pos(),
            )

        val errors = validator.check(PrintStatement(expression, pos()), env)

        assertEquals("Variable 'uninitialized' is not initialized", errors.single().message)
    }
}
