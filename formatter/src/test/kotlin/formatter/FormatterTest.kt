package formatter

import ast.Assignment
import ast.BinaryExpression
import ast.BooleanLiteral
import ast.Identifier
import ast.IfStatement
import ast.NumberLiteral
import ast.Position
import ast.PrintStatement
import ast.Program
import ast.StringLiteral
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FormatterTest {
    private val formatter = PrintScriptFormatter()

    @Test
    fun `format simple variable declaration`() {
        val stmt =
            VariableDeclaration(
                name = "x",
                type = "number",
                value = NumberLiteral(5.0, Position(1, 1)),
                position = Position(1, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(program)

        assertTrue(result.contains("let x : number = 5.0;"))
    }

    @Test
    fun `format variable declaration without value`() {
        val stmt =
            VariableDeclaration(
                name = "name",
                type = "string",
                value = null,
                position = Position(1, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(program)

        assertTrue(result.contains("let name : string;"))
    }

    @Test
    fun `format assignment statement`() {
        val stmt =
            Assignment(
                name = "x",
                value = NumberLiteral(10.0, Position(1, 1)),
                position = Position(1, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(program)

        assertTrue(result.contains("x = 10.0;"))
    }

    @Test
    fun `format binary expression in assignment`() {
        val expr =
            BinaryExpression(
                left = Identifier("a", Position(1, 1)),
                operator = "+",
                right = Identifier("b", Position(1, 1)),
                position = Position(1, 1),
            )
        val stmt =
            Assignment(
                name = "result",
                value = expr,
                position = Position(1, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(program)

        assertTrue(result.contains("result = a + b;"))
    }

    @Test
    fun `format print statement`() {
        val stmt =
            PrintStatement(
                argument = Identifier("x", Position(1, 1)),
                position = Position(1, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(program)

        assertTrue(result.contains("println(x);"))
    }

    @Test
    fun `format boolean literal`() {
        val stmt = PrintStatement(BooleanLiteral(true, Position(1, 1)), Position(1, 1))
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(program)

        assertEquals("println(true);", result)
    }

    @Test
    fun `format print with binary expression`() {
        val expr =
            BinaryExpression(
                left = NumberLiteral(5.0, Position(1, 1)),
                operator = "+",
                right = NumberLiteral(3.0, Position(1, 1)),
                position = Position(1, 1),
            )
        val stmt = PrintStatement(expr, Position(1, 1))
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(program)

        assertTrue(result.contains("println(5.0 + 3.0);"))
    }

    @Test
    fun `format string concatenation`() {
        val expr =
            BinaryExpression(
                left = StringLiteral("Hello ", Position(1, 1)),
                operator = "+",
                right = Identifier("name", Position(1, 1)),
                position = Position(1, 1),
            )
        val stmt = PrintStatement(expr, Position(1, 1))
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(program)

        assertTrue(result.contains("println(\"Hello \" + name);"))
    }

    @Test
    fun `format multiple statements`() {
        val stmt1 =
            VariableDeclaration(
                name = "x",
                type = "number",
                value = NumberLiteral(5.0, Position(1, 1)),
                position = Position(1, 1),
            )
        val stmt2 =
            Assignment(
                name = "x",
                value = NumberLiteral(10.0, Position(1, 1)),
                position = Position(2, 1),
            )
        val stmt3 =
            PrintStatement(
                argument = Identifier("x", Position(3, 1)),
                position = Position(3, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt1, stmt2, stmt3))

        val result = formatter.format(program)

        assertTrue(result.contains("let x : number = 5.0;"))
        assertTrue(result.contains("x = 10.0;"))
        assertTrue(result.contains("println(x);"))
    }

    @Test
    fun `format with custom rules - no spaces around equal`() {
        val rules = FormattingRules(spaceAroundEqual = false)
        val formatter = PrintScriptFormatter(rules)

        val stmt =
            Assignment(
                name = "x",
                value = NumberLiteral(5.0, Position(1, 1)),
                position = Position(1, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(program)

        assertTrue(result.contains("x=5.0;"))
        assertFalse(result.contains("x = 5.0;"))
    }

    @Test
    fun `format if block with default indentation`() {
        val statement =
            IfStatement(
                condition = Identifier("enabled", Position(1, 1)),
                thenBranch = listOf(PrintStatement(StringLiteral("on", Position(1, 1)), Position(1, 1))),
                elseBranch = null,
                position = Position(1, 1),
            )

        val result = formatter.format(Program(Position(1, 1), listOf(statement)))

        assertEquals(
            """
            if (enabled) {
                println("on");
            }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `format if else blocks`() {
        val statement =
            IfStatement(
                condition = Identifier("enabled", Position(1, 1)),
                thenBranch = listOf(PrintStatement(StringLiteral("on", Position(1, 1)), Position(1, 1))),
                elseBranch = listOf(PrintStatement(StringLiteral("off", Position(1, 1)), Position(1, 1))),
                position = Position(1, 1),
            )

        val result = formatter.format(Program(Position(1, 1), listOf(statement)))

        assertEquals(
            """
            if (enabled) {
                println("on");
            } else {
                println("off");
            }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `format nested if blocks`() {
        val nested =
            IfStatement(
                condition = Identifier("secondary", Position(1, 1)),
                thenBranch = listOf(PrintStatement(StringLiteral("nested", Position(1, 1)), Position(1, 1))),
                elseBranch = null,
                position = Position(1, 1),
            )
        val statement =
            IfStatement(
                condition = Identifier("primary", Position(1, 1)),
                thenBranch = listOf(nested),
                elseBranch = null,
                position = Position(1, 1),
            )

        val result = formatter.format(Program(Position(1, 1), listOf(statement)))

        assertEquals(
            """
            if (primary) {
                if (secondary) {
                    println("nested");
                }
            }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `format if block with configured indentation`() {
        val formatter = PrintScriptFormatter(FormattingRules(indentationSpaces = 2))
        val statement =
            IfStatement(
                condition = Identifier("enabled", Position(1, 1)),
                thenBranch = listOf(PrintStatement(StringLiteral("on", Position(1, 1)), Position(1, 1))),
                elseBranch = null,
                position = Position(1, 1),
            )

        val result = formatter.format(Program(Position(1, 1), listOf(statement)))

        assertEquals(
            """
            if (enabled) {
              println("on");
            }
            """.trimIndent(),
            result,
        )
    }
}
