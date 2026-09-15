package parser.grammar

import ast.BooleanLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.engine.ParseResult
import parser.tok
import token.TokenType.EOF
import token.TokenType.TRUE

class GrammarExpressionV11Test {
    private val expression = ExpressionRule(extraPrimaries = listOf(BooleanLiteralRule.rule)).expression

    @Test
    fun `expression with extra primaries parses boolean literals`() {
        val tokens = listOf(tok(TRUE, "true"), tok(EOF))

        val result = expression.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        assertEquals(true, ((result as ParseResult.Success).value as BooleanLiteral).value)
    }

    @Test
    fun `default expression without extra primaries rejects boolean literals`() {
        val tokens = listOf(tok(TRUE, "true"), tok(EOF))

        val result = ExpressionRule().expression.parse(tokens, 0)

        assertTrue(result is ParseResult.Failure)
    }
}
