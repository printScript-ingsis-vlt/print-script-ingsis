package rules

import dataclass.Severity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import recurses.BinaryExpression
import recurses.Identifier
import recurses.NumberLiteral
import recurses.Position
import recurses.PrintStatement
import recurses.Program
import recurses.StringLiteral

class PrintlnArgumentRuleTest {

    private val rule = PrintlnArgumentRule()

    @Test
    fun `should accept println with identifier argument`() {
        val stmt = PrintStatement(
            argument = Identifier("result", Position(1, 9)),
            position = Position(1, 1)
        )
        val program = Program(Position(1, 1), listOf(stmt))

        val notifications = rule.check(program)

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `should accept println with number literal argument`() {
        val stmt = PrintStatement(
            argument = NumberLiteral(123.0, Position(1, 9)),
            position = Position(1, 1)
        )
        val program = Program(Position(1, 1), listOf(stmt))

        val notifications = rule.check(program)

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `should accept println with string literal argument`() {
        val stmt = PrintStatement(
            argument = StringLiteral("Hello World", Position(1, 9)),
            position = Position(1, 1)
        )
        val program = Program(Position(1, 1), listOf(stmt))

        val notifications = rule.check(program)

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `should report warning when println argument is a binary expression`() {
        val expr = BinaryExpression(
            left = StringLiteral("Result: ", Position(1, 9)),
            operator = "+",
            right = Identifier("c", Position(1, 21)),
            position = Position(1, 9)
        )
        val stmt = PrintStatement(
            argument = expr,
            position = Position(1, 1)
        )
        val program = Program(Position(1, 1), listOf(stmt))

        val notifications = rule.check(program)

        assertEquals(1, notifications.size)
        assertEquals("println-argument", notifications[0].rule)
        assertEquals(Severity.WARNING, notifications[0].severity)
        assertEquals(Position(1, 9), notifications[0].position)
    }
}
