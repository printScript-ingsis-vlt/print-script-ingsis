package interpreter

import ast.Position
import ast.ReadEnvExpression
import ast.StringLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import runtime.valuedataclass.NumberValue
import runtime.valuedataclass.StringValue

class ReadEnvTest {
    private fun pos() = Position(1, 1)

    private class TestOutput : Output {
        override fun write(message: String) = Unit
    }

    @Test
    fun `readEnv retorna valor de variable de ambiente`() {
        val mockEnv = MockEnvProvider(mapOf("PORT" to "8080"))
        val interpreter = ConfigurableInterpreter(TestOutput(), envProvider = mockEnv)

        val expr = ReadEnvExpression(StringLiteral("PORT", pos()), pos())
        val result = interpreter.evaluate(expr)

        assertEquals(NumberValue(8080.0), result)
    }

    @Test
    fun `readEnv falla si variable no existe`() {
        val mockEnv = MockEnvProvider(emptyMap())
        val interpreter = ConfigurableInterpreter(TestOutput(), envProvider = mockEnv)

        val expr = ReadEnvExpression(StringLiteral("NON_EXISTENT_VAR", pos()), pos())

        assertThrows(IllegalStateException::class.java) {
            interpreter.evaluate(expr)
        }
    }

    @Test
    fun `readEnv parsea como tipo esperado`() {
        val mockEnv = MockEnvProvider(mapOf("API_URL" to "https://api.example.com"))
        val interpreter = ConfigurableInterpreter(TestOutput(), envProvider = mockEnv)

        val expr = ReadEnvExpression(StringLiteral("API_URL", pos()), pos())
        val result = interpreter.evaluate(expr)

        assertEquals(StringValue("https://api.example.com"), result)
    }
}
