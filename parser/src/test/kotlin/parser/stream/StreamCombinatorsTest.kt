package parser.stream

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.engine.ListTokenCursor
import parser.tok
import token.Token
import token.TokenType

class StreamCombinatorsTest {
    @Test
    fun `TokenRule success and failure`() {
        val rule = token(TokenType.LET, "let")

        val cursorSuccess = ListTokenCursor(listOf(tok(TokenType.LET, "let")))
        val resSuccess = rule.parse(cursorSuccess)
        assertTrue(resSuccess is StreamParseResult.Success)
        assertEquals("let", ((resSuccess as StreamParseResult.Success).value as Token).value)

        val cursorFail = ListTokenCursor(listOf(tok(TokenType.IDENTIFIER, "x")))
        val resFail = rule.parse(cursorFail)
        assertTrue(resFail is StreamParseResult.Failure)
    }

    @Test
    fun `LiteralRule success and failure`() {
        val rule = literal(TokenType.IDENTIFIER, "println")

        val cursorSuccess = ListTokenCursor(listOf(tok(TokenType.IDENTIFIER, "println")))
        assertTrue(rule.parse(cursorSuccess) is StreamParseResult.Success)

        val cursorFail = ListTokenCursor(listOf(tok(TokenType.IDENTIFIER, "print")))
        assertTrue(rule.parse(cursorFail) is StreamParseResult.Failure)
    }

    @Test
    fun `Seq success and failure`() {
        val rule = seq(token(TokenType.LET), token(TokenType.IDENTIFIER))

        val cursorSuccess = ListTokenCursor(listOf(tok(TokenType.LET), tok(TokenType.IDENTIFIER, "x")))
        val resSuccess = rule.parse(cursorSuccess)
        assertTrue(resSuccess is StreamParseResult.Success)

        val cursorFail = ListTokenCursor(listOf(tok(TokenType.LET), tok(TokenType.NUMBER_LITERAL, "1")))
        val resFail = rule.parse(cursorFail)
        assertTrue(resFail is StreamParseResult.Failure)
    }

    @Test
    fun `Choice success and backtracking on failure`() {
        val rule =
            choice(
                seq(
                    token(TokenType.LET),
                    token(TokenType.COLON),
                ),
                seq(
                    token(TokenType.LET),
                    token(TokenType.IDENTIFIER),
                ),
            )

        val cursor = ListTokenCursor(listOf(tok(TokenType.LET), tok(TokenType.IDENTIFIER, "x")))
        val result = rule.parse(cursor)
        assertTrue(result is StreamParseResult.Success)
        assertEquals(2, cursor.position())
    }

    @Test
    fun `Many matches zero or multiple`() {
        val rule = many(token(TokenType.IDENTIFIER))

        val cursorEmpty =
            ListTokenCursor(
                listOf(
                    tok(TokenType.SEMICOLON),
                ),
            )
        val resEmpty = rule.parse(cursorEmpty) as StreamParseResult.Success
        assertEquals(0, (resEmpty.value as List<*>).size)
        assertEquals(0, cursorEmpty.position())

        val cursorMany =
            ListTokenCursor(
                listOf(
                    tok(TokenType.IDENTIFIER),
                    tok(TokenType.IDENTIFIER),
                    tok(TokenType.SEMICOLON),
                ),
            )
        val resMany = rule.parse(cursorMany) as StreamParseResult.Success
        assertEquals(2, (resMany.value as List<*>).size)
        assertEquals(2, cursorMany.position())
    }

    @Test
    fun `Opt matches when present or returns null when absent`() {
        val rule = opt(token(TokenType.EQUAL))

        val cursorPresent = ListTokenCursor(listOf(tok(TokenType.EQUAL)))
        val resPresent = rule.parse(cursorPresent) as StreamParseResult.Success
        assertNotNull(resPresent.value)
        assertEquals(1, cursorPresent.position())

        val cursorAbsent = ListTokenCursor(listOf(tok(TokenType.SEMICOLON)))
        val resAbsent = rule.parse(cursorAbsent) as StreamParseResult.Success
        assertNull(resAbsent.value)
        assertEquals(0, cursorAbsent.position())
    }

    @Test
    fun `Action transforms parsed result`() {
        val rule = action(token(TokenType.NUMBER_LITERAL)) { (it as Token).value.toDouble() }
        val cursor = ListTokenCursor(listOf(tok(TokenType.NUMBER_LITERAL, "42")))
        val result = rule.parse(cursor) as StreamParseResult.Success
        assertEquals(42.0, result.value)
    }

    @Test
    fun `Ref resolves recursively`() {
        lateinit var expr: StreamRule
        expr =
            choice(
                seq(token(TokenType.LEFT_PAREN), ref { expr }, token(TokenType.RIGHT_PAREN)),
                token(TokenType.IDENTIFIER),
            )

        val tokens =
            listOf(
                tok(TokenType.LEFT_PAREN),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.RIGHT_PAREN),
            )
        val cursor = ListTokenCursor(tokens)
        val result = expr.parse(cursor)
        assertTrue(result is StreamParseResult.Success)
        assertEquals(3, cursor.position())
    }
}
