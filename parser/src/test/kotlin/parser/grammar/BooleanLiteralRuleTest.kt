package parser.grammar

import ast.BooleanLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.engine.ParseResult
import parser.tok
import token.TokenType.EOF
import token.TokenType.FALSE
import token.TokenType.TRUE

class BooleanLiteralRuleTest {
    @Test
    fun `parses true`() {
        val tokens = listOf(tok(TRUE, "true"), tok(EOF))

        val result = BooleanLiteralRule.rule.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val literal = (result as ParseResult.Success).value as BooleanLiteral
        assertEquals(true, literal.value)
    }

    @Test
    fun `parses false`() {
        val tokens = listOf(tok(FALSE, "false"), tok(EOF))

        val result = BooleanLiteralRule.rule.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val literal = (result as ParseResult.Success).value as BooleanLiteral
        assertEquals(false, literal.value)
    }

    @Test
    fun `fails on anything else`() {
        val tokens = listOf(tok(EOF))

        val result = BooleanLiteralRule.rule.parse(tokens, 0)

        assertTrue(result is ParseResult.Failure)
    }
}
