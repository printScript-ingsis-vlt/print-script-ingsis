import config.IdentifierFormat
import config.LintConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import recurses.BinaryExpression
import recurses.Identifier
import recurses.NumberLiteral
import recurses.Position
import recurses.PrintStatement
import recurses.Program
import recurses.VariableDeclaration

class PrintScriptLinterTest {

    @Test
    fun `should report no warnings for clean program with default config`() {
        val linter = PrintScriptLinter()

        val stmt1 = VariableDeclaration(
            name = "totalCount",
            type = "number",
            value = NumberLiteral(10.0, Position(1, 25)),
            position = Position(1, 1)
        )
        val stmt2 = PrintStatement(
            argument = Identifier("totalCount", Position(2, 9)),
            position = Position(2, 1)
        )
        val program = Program(Position(1, 1), listOf(stmt1, stmt2))

        val notifications = linter.lint(program)

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `should not report binary expression in println when check is turned off`() {
        val config = LintConfig(
            identifierFormat = IdentifierFormat.CAMEL_CASE,
            printlnArgumentCheck = false
        )
        val linter = PrintScriptLinter(config)

        val expr = BinaryExpression(
            left = NumberLiteral(1.0, Position(1, 9)),
            operator = "+",
            right = NumberLiteral(2.0, Position(1, 13)),
            position = Position(1, 9)
        )
        val stmt = PrintStatement(expr, Position(1, 1))
        val program = Program(Position(1, 1), listOf(stmt))

        val notifications = linter.lint(program)

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `should report multiple violations when rules are violated`() {
        val config = LintConfig(
            identifierFormat = IdentifierFormat.CAMEL_CASE,
            printlnArgumentCheck = true
        )
        val linter = PrintScriptLinter(config)

        val stmt1 = VariableDeclaration(
            name = "my_var",
            type = "number",
            value = NumberLiteral(5.0, Position(1, 20)),
            position = Position(1, 1)
        )
        val expr = BinaryExpression(
            left = Identifier("my_var", Position(2, 9)),
            operator = "+",
            right = NumberLiteral(1.0, Position(2, 18)),
            position = Position(2, 9)
        )
        val stmt2 = PrintStatement(expr, Position(2, 1))

        val program = Program(Position(1, 1), listOf(stmt1, stmt2))

        val notifications = linter.lint(program)

        assertEquals(2, notifications.size)
        assertEquals("identifier-format", notifications[0].rule)
        assertEquals("println-argument", notifications[1].rule)
    }
}
