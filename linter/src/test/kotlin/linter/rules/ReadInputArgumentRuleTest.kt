package linter.rules

import ast.BinaryExpression
import ast.Expr
import ast.Identifier
import ast.NumberLiteral
import ast.Position
import ast.PrintStatement
import ast.Program
import ast.ReadInputExpression
import ast.StringLiteral
import linter.dataclass.Severity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReadInputArgumentRuleTest {
    private val rule = ReadInputArgumentRule()
    private val position = Position(1, 1)

    @Test
    fun `accepts readInput with a string literal`() {
        val notifications = rule.check(programWithReadInput(StringLiteral("Enter: ", position)))

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `accepts readInput with an identifier`() {
        val notifications = rule.check(programWithReadInput(Identifier("prompt", position)))

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `reports readInput with string concatenation`() {
        val prompt = BinaryExpression(StringLiteral("Enter: ", position), "+", Identifier("suffix", position), position)

        val notifications = rule.check(programWithReadInput(prompt))

        assertInvalidPromptNotification(notifications)
    }

    @Test
    fun `reports readInput with a numeric expression`() {
        val prompt = BinaryExpression(NumberLiteral(1.0, position), "+", NumberLiteral(2.0, position), position)

        val notifications = rule.check(programWithReadInput(prompt))

        assertInvalidPromptNotification(notifications)
    }

    private fun programWithReadInput(prompt: Expr): Program =
        Program(
            position,
            listOf(PrintStatement(ReadInputExpression(prompt, position), position)),
        )

    private fun assertInvalidPromptNotification(notifications: List<linter.dataclass.LintNotification>) {
        assertEquals(1, notifications.size)
        assertEquals("read-input-argument", notifications.single().rule)
        assertEquals(Severity.ERROR, notifications.single().severity)
        assertEquals(position, notifications.single().position)
    }
}
