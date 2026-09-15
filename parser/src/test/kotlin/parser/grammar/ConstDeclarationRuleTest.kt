package parser.grammar

import ast.NumberLiteral
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.engine.ParseResult
import parser.tok
import token.TokenType.COLON
import token.TokenType.CONST
import token.TokenType.EOF
import token.TokenType.EQUAL
import token.TokenType.IDENTIFIER
import token.TokenType.NUMBER_LITERAL
import token.TokenType.SEMICOLON

class ConstDeclarationRuleTest {
    private val rule = ConstDeclarationRule(ExpressionRule().expression).rule

    @Test
    fun `parses a const declaration with an initializer`() {
        // const x: Number = 42;
        val tokens =
            listOf(
                tok(CONST, "const"),
                tok(IDENTIFIER, "x"),
                tok(COLON, ":"),
                tok(IDENTIFIER, "Number"),
                tok(EQUAL, "="),
                tok(NUMBER_LITERAL, "42"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = rule.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val stmt = (result as ParseResult.Success).value as VariableDeclaration
        assertEquals("x", stmt.name)
        assertEquals("Number", stmt.type)
        assertTrue(stmt.value is NumberLiteral)
        assertFalse(stmt.mutable)
    }

    @Test
    fun `fails without an initializer`() {
        // const x: Number;
        val tokens =
            listOf(
                tok(CONST, "const"),
                tok(IDENTIFIER, "x"),
                tok(COLON, ":"),
                tok(IDENTIFIER, "Number"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = rule.parse(tokens, 0)
        assertTrue(result is ParseResult.Failure)
    }

    @Test
    fun `fails without the equal sign`() {
        val tokens =
            listOf(
                tok(CONST, "const"),
                tok(IDENTIFIER, "x"),
                tok(COLON, ":"),
                tok(IDENTIFIER, "Number"),
                tok(NUMBER_LITERAL, "42"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = rule.parse(tokens, 0)
        assertTrue(result is ParseResult.Failure)
    }
}
