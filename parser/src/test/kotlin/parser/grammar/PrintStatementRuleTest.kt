package parser.grammar

import ast.Identifier
import ast.PrintStatement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.engine.ParseResult
import parser.tok
import token.TokenType.EOF
import token.TokenType.IDENTIFIER
import token.TokenType.LEFT_PAREN
import token.TokenType.RIGHT_PAREN
import token.TokenType.SEMICOLON

class PrintStatementRuleTest {
    private val rule = PrintStatementRule(ExpressionRule.expression).rule

    @Test
    fun `parsea println con un identificador`() {
        // println(x);
        val tokens =
            listOf(
                tok(IDENTIFIER, "println"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = rule.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val stmt = (result as ParseResult.Success).value as PrintStatement
        assertEquals("x", (stmt.argument as Identifier).name)
        assertEquals(5, result.next)
    }

    @Test
    fun `falla si el identificador no es exactamente println`() {
        val tokens =
            listOf(
                tok(IDENTIFIER, "print"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = rule.parse(tokens, 0)

        assertTrue(result is ParseResult.Failure)
    }

    @Test
    fun `falla sin parentesis`() {
        val tokens =
            listOf(
                tok(IDENTIFIER, "println"),
                tok(IDENTIFIER, "x"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = rule.parse(tokens, 0)

        assertTrue(result is ParseResult.Failure)
    }
}
