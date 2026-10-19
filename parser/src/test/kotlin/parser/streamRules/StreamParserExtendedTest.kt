package parser.streamRules

import ast.BinaryExpression
import ast.NumberLiteral
import ast.PrintScriptVersion
import ast.Program
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.engine.ListTokenCursor
import parser.tok
import result.Result
import token.TokenType

class StreamParserExtendedTest {
    @Test
    fun `test operator precedence - multiplication has higher precedence than addition`() {
        val parser = StreamGrammarConfigurations.getConfiguration(PrintScriptVersion.V1_0)
        // let x: number = 2 + 3 * 4;
        val tokens =
            listOf(
                tok(TokenType.LET),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.COLON),
                tok(TokenType.IDENTIFIER, "number"),
                tok(TokenType.EQUAL),
                tok(TokenType.NUMBER_LITERAL, "2"),
                tok(TokenType.PLUS, "+"),
                tok(TokenType.NUMBER_LITERAL, "3"),
                tok(TokenType.STAR, "*"),
                tok(TokenType.NUMBER_LITERAL, "4"),
                tok(TokenType.SEMICOLON),
                tok(TokenType.EOF),
            )

        val result = parser.parse(tokens)
        assertTrue(result is Result.Success)
        val program = (result as Result.Success).value
        val decl = program.statements[0] as VariableDeclaration

        // Raíz debe ser '+' con izquierda=2 y derecha=(3 * 4)
        val root = decl.value as BinaryExpression
        assertEquals("+", root.operator)
        assertEquals(2.0, (root.left as NumberLiteral).value)

        val right = root.right as BinaryExpression
        assertEquals("*", right.operator)
        assertEquals(3.0, (right.left as NumberLiteral).value)
        assertEquals(4.0, (right.right as NumberLiteral).value)
    }

    @Test
    fun `test parentheses override precedence`() {
        val parser = StreamGrammarConfigurations.getConfiguration(PrintScriptVersion.V1_0)
        // let x: number = (2 + 3) * 4;
        val tokens =
            listOf(
                tok(TokenType.LET),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.COLON),
                tok(TokenType.IDENTIFIER, "number"),
                tok(TokenType.EQUAL),
                tok(TokenType.LEFT_PAREN),
                tok(TokenType.NUMBER_LITERAL, "2"),
                tok(TokenType.PLUS, "+"),
                tok(TokenType.NUMBER_LITERAL, "3"),
                tok(TokenType.RIGHT_PAREN),
                tok(TokenType.STAR, "*"),
                tok(TokenType.NUMBER_LITERAL, "4"),
                tok(TokenType.SEMICOLON),
                tok(TokenType.EOF),
            )

        val result = parser.parse(tokens)
        assertTrue(result is Result.Success)
        val program = (result as Result.Success).value
        val decl = program.statements[0] as VariableDeclaration

        // Raíz debe ser '*' con izquierda=(2 + 3) y derecha=4
        val root = decl.value as BinaryExpression
        assertEquals("*", root.operator)
        val left = root.left as BinaryExpression
        assertEquals("+", left.operator)
        assertEquals(4.0, (root.right as NumberLiteral).value)
    }

    @Test
    fun `test declaration without initializer`() {
        val parser = StreamGrammarConfigurations.getConfiguration(PrintScriptVersion.V1_0)
        // let x: number;
        val tokens =
            listOf(
                tok(TokenType.LET),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.COLON),
                tok(TokenType.IDENTIFIER, "number"),
                tok(TokenType.SEMICOLON),
                tok(TokenType.EOF),
            )

        val result = parser.parse(tokens)
        assertTrue(result is Result.Success)
        val decl = (result as Result.Success).value.statements[0] as VariableDeclaration
        assertEquals("x", decl.name)
        assertNull(decl.value)
    }

    @Test
    fun `test syntax error recovery skips to semicolon`() {
        val parser = StreamGrammarConfigurations.getConfiguration(PrintScriptVersion.V1_0)
        // let = ;
        // let y: number = 10;
        val tokens =
            listOf(
                tok(TokenType.LET),
                tok(TokenType.EQUAL),
                tok(TokenType.SEMICOLON),
                tok(TokenType.LET),
                tok(TokenType.IDENTIFIER, "y"),
                tok(TokenType.COLON),
                tok(TokenType.IDENTIFIER, "number"),
                tok(TokenType.EQUAL),
                tok(TokenType.NUMBER_LITERAL, "10"),
                tok(TokenType.SEMICOLON),
                tok(TokenType.EOF),
            )

        val result = parser.parse(tokens)
        assertTrue(result is Result.Failure)
        val errors = (result as Result.Failure).error
        assertEquals(1, errors.size)
    }

    @Test
    fun `test parse with TokenCursor overload`() {
        val parser = StreamGrammarConfigurations.default
        val tokens =
            listOf(
                tok(TokenType.LET),
                tok(TokenType.IDENTIFIER, "a"),
                tok(TokenType.COLON),
                tok(TokenType.IDENTIFIER, "string"),
                tok(TokenType.EQUAL),
                tok(TokenType.STRING_LITERAL, "hello"),
                tok(TokenType.SEMICOLON),
                tok(TokenType.EOF),
            )
        val cursor = ListTokenCursor(tokens)
        val result = parser.parse(cursor)
        assertTrue(result is Result.Success<*>)
        val program = (result as Result.Success<Program>).value
        assertEquals(1, program.statements.size)
    }
}
