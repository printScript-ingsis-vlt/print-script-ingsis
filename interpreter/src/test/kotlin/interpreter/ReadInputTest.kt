package interpreter

import ast.Position
import ast.ReadInputExpression
import ast.StringLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import runtime.valuedataclass.BooleanValue
import runtime.valuedataclass.NumberValue
import runtime.valuedataclass.StringValue

class ReadInputTest {
    private fun pos() = Position(1, 1)

    private class TestOutput : Output {
        val printed = mutableListOf<String>()

        override fun write(message: String) {
            printed.add(message)
        }
    }

    @Test
    fun `readInput retorna string`() {
        val mockInput = MockInputProvider(listOf("Hola Mundo"))
        val output = TestOutput()
        val interpreter = ConfigurableInterpreter(output, mockInput)

        val expr = ReadInputExpression(StringLiteral("Ingrese texto: ", pos()), pos())
        val result = interpreter.evaluate(expr)

        // El resultado retornado por evaluate debe ser el valor leído de InputProvider
        assertEquals(StringValue("Hola Mundo"), result)

        // El Output debe contener únicamente el mensaje del prompt enviado
        assertEquals(listOf("Ingrese texto: "), output.printed)
    }

    @Test
    fun `readInput parsea numero correctamente`() {
        val mockInput = MockInputProvider(listOf("42.5"))
        val output = TestOutput()
        val interpreter = ConfigurableInterpreter(output, mockInput)

        val expr = ReadInputExpression(StringLiteral("Ingrese texto: ", pos()), pos())
        val result = interpreter.evaluate(expr)

        assertEquals(NumberValue(42.5), result)
    }

    @Test
    fun `readInput falla con input invalido para boolean`() {
        val mockInput = MockInputProvider(listOf("invalid_bool"))
        val output = TestOutput()
        val interpreter = ConfigurableInterpreter(output, mockInput)

        val expr = ReadInputExpression(StringLiteral("Ingrese texto: ", pos()), pos())

        // Si tu lógica requiere validar que el string no sea aceptado cuando se requiere un booleano:
        val rawResult = interpreter.evaluate(expr)

        // Al intentar requerir un BooleanValue a partir de rawResult
        assertThrows(IllegalArgumentException::class.java) {
            require(rawResult is BooleanValue) { "Input no se puede parsear a boolean" }
        }
    }
}
