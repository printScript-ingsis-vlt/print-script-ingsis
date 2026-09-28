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
import ast.ReadEnvExpression
import ast.ReadInputExpression
import ast.StringLiteral
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import token.Token
import token.TokenType

class FormatterTest {
    // Reglas explícitas (no default = null) para no depender de tokens reales en estos tests,
    // que construyen el AST a mano sin un archivo fuente real.
    private val formatter =
        PrintScriptFormatter(
            FormattingRules(spaceBeforeColon = true, spaceAfterColon = true, spaceAroundEqual = true),
        )

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

        val result = formatter.format(emptyList(), program)

        assertTrue(result.contains("let x : number = 5;"))
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

        val result = formatter.format(emptyList(), program)

        assertTrue(result.contains("let name : string;"))
    }

    @Test
    fun `format constant declaration`() {
        val stmt =
            VariableDeclaration(
                name = "port",
                type = "number",
                value = NumberLiteral(8080.0, Position(1, 1)),
                position = Position(1, 1),
                mutable = false,
            )

        val result = formatter.format(emptyList(), Program(Position(1, 1), listOf(stmt)))

        assertEquals("const port : number = 8080;", result)
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

        val result = formatter.format(emptyList(), program)

        assertTrue(result.contains("x = 10;"))
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

        val result = formatter.format(emptyList(), program)

        assertTrue(result.contains("result = a + b;"))
    }

    @Test
    fun `format number literal without trailing zero for whole numbers`() {
        val stmt = PrintStatement(NumberLiteral(8080.0, Position(1, 1)), Position(1, 1))
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(emptyList(), program)

        assertEquals("println(8080);", result)
    }

    @Test
    fun `format number literal keeps decimals when they are not zero`() {
        val stmt = PrintStatement(NumberLiteral(5.5, Position(1, 1)), Position(1, 1))
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(emptyList(), program)

        assertEquals("println(5.5);", result)
    }

    @Test
    fun `format print statement`() {
        val stmt =
            PrintStatement(
                argument = Identifier("x", Position(1, 1)),
                position = Position(1, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(emptyList(), program)

        assertTrue(result.contains("println(x);"))
    }

    @Test
    fun `format boolean literal`() {
        val stmt = PrintStatement(BooleanLiteral(true, Position(1, 1)), Position(1, 1))
        val program = Program(Position(1, 1), listOf(stmt))

        val result = formatter.format(emptyList(), program)

        assertEquals("println(true);", result)
    }

    @Test
    fun `format readInput expression`() {
        val expression = ReadInputExpression(StringLiteral("Name", Position(1, 1)), Position(1, 1))
        val statement = PrintStatement(expression, Position(1, 1))

        val result = formatter.format(emptyList(), Program(Position(1, 1), listOf(statement)))

        assertEquals("println(readInput(\"Name\"));", result)
    }

    @Test
    fun `format readEnv expression`() {
        val expression = ReadEnvExpression(Identifier("variableName", Position(1, 1)), Position(1, 1))
        val statement = PrintStatement(expression, Position(1, 1))

        val result = formatter.format(emptyList(), Program(Position(1, 1), listOf(statement)))

        assertEquals("println(readEnv(variableName));", result)
    }

    @Test
    fun `format readInput inside a binary expression`() {
        val expression =
            BinaryExpression(
                left = StringLiteral("Name: ", Position(1, 1)),
                operator = "+",
                right = ReadInputExpression(StringLiteral("Enter name", Position(1, 1)), Position(1, 1)),
                position = Position(1, 1),
            )
        val statement = PrintStatement(expression, Position(1, 1))

        val result = formatter.format(emptyList(), Program(Position(1, 1), listOf(statement)))

        assertEquals("println(\"Name: \" + readInput(\"Enter name\"));", result)
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

        val result = formatter.format(emptyList(), program)

        assertTrue(result.contains("println(5 + 3);"))
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

        val result = formatter.format(emptyList(), program)

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

        val result = formatter.format(emptyList(), program)

        assertTrue(result.contains("let x : number = 5;"))
        assertTrue(result.contains("x = 10;"))
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

        val result = formatter.format(emptyList(), program)

        assertTrue(result.contains("x=5;"))
        assertFalse(result.contains("x = 5;"))
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

        val result = formatter.format(emptyList(), Program(Position(1, 1), listOf(statement)))

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
    fun `format constant with readEnv inside an if block`() {
        val declaration =
            VariableDeclaration(
                name = "port",
                type = "number",
                value = ReadEnvExpression(StringLiteral("PORT", Position(1, 1)), Position(1, 1)),
                position = Position(1, 1),
                mutable = false,
            )
        val statement =
            IfStatement(
                condition = Identifier("enabled", Position(1, 1)),
                thenBranch = listOf(declaration),
                elseBranch = null,
                position = Position(1, 1),
            )

        val result = formatter.format(emptyList(), Program(Position(1, 1), listOf(statement)))

        assertEquals(
            """
            if (enabled) {
                const port : number = readEnv("PORT");
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

        val result = formatter.format(emptyList(), Program(Position(1, 1), listOf(statement)))

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

        val result = formatter.format(emptyList(), Program(Position(1, 1), listOf(statement)))

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

        val result = formatter.format(emptyList(), Program(Position(1, 1), listOf(statement)))

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
    fun `preserves original spacing in a declaration when rules are null`() {
        // fuente equivalente: let  x   :string   =\"Joe\";
        val stmt =
            VariableDeclaration(
                name = "x",
                type = "string",
                value = StringLiteral("Joe", Position(1, 20)),
                position = Position(1, 1),
            )
        val tokens =
            listOf(
                Token(TokenType.LET, "let", Position(1, 1), Position(1, 4)),
                Token(TokenType.IDENTIFIER, "x", Position(1, 5), Position(1, 6)),
                Token(TokenType.COLON, ":", Position(1, 9), Position(1, 10)),
                Token(TokenType.IDENTIFIER, "string", Position(1, 10), Position(1, 16)),
                Token(TokenType.EQUAL, "=", Position(1, 19), Position(1, 20)),
                Token(TokenType.STRING_LITERAL, "\"Joe\"", Position(1, 20), Position(1, 25)),
                Token(TokenType.SEMICOLON, ";", Position(1, 25), Position(1, 26)),
            )
        val formatter = PrintScriptFormatter(FormattingRules.default())

        val result = formatter.format(tokens, Program(Position(1, 1), listOf(stmt)))

        assertEquals("let x   :string   =\"Joe\";", result)
    }

    @Test
    fun `preserves original spacing in an assignment when rules are null`() {
        // fuente equivalente: x   =  5;
        val stmt =
            Assignment(
                name = "x",
                value = NumberLiteral(5.0, Position(1, 8)),
                position = Position(1, 1),
            )
        val tokens =
            listOf(
                Token(TokenType.IDENTIFIER, "x", Position(1, 1), Position(1, 2)),
                Token(TokenType.EQUAL, "=", Position(1, 5), Position(1, 6)),
                Token(TokenType.NUMBER_LITERAL, "5", Position(1, 8), Position(1, 9)),
                Token(TokenType.SEMICOLON, ";", Position(1, 9), Position(1, 10)),
            )
        val formatter = PrintScriptFormatter(FormattingRules.default())

        val result = formatter.format(tokens, Program(Position(1, 1), listOf(stmt)))

        assertEquals("x   =  5;", result)
    }
}
