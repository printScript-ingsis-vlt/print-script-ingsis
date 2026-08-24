package engine

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import recurses.TokenType
import tok

class EngineTest {

    // ---------- TokenRule ----------
    @Test
    fun `TokenRule success`() {
        val rule = token(TokenType.LET, "let")
        val tokens = listOf(tok(TokenType.LET, "let"), tok(TokenType.EOF))

        val result = rule.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(1, success.next)
        assertEquals("let", (success.value as recurses.Token).value)
    }

    @Test
    fun `TokenRule failure`() {
        val rule = token(TokenType.LET, "let")
        val tokens = listOf(tok(TokenType.IDENTIFIER, "x"), tok(TokenType.EOF))

        val result = rule.parse(tokens, 0)

        assertTrue(result is ParseResult.Failure)
    }

    // ---------- Seq ----------
    @Test
    fun `Seq success`() {
        val rule = seq(
            token(TokenType.LET),
            token(TokenType.IDENTIFIER)
        )
        val tokens = listOf(
            tok(TokenType.LET, "let"),
            tok(TokenType.IDENTIFIER, "x"),
            tok(TokenType.EOF)
        )

        val result = rule.parse(tokens, 0) as ParseResult.Success
        val values = result.value as List<*>

        assertEquals(2, values.size)
        assertEquals(2, result.next)
    }

    @Test
    fun `Seq failure on second rule`() {
        val rule = seq(
            token(TokenType.LET),
            token(TokenType.IDENTIFIER)
        )
        val tokens = listOf(
            tok(TokenType.LET, "let"),
            tok(TokenType.NUMBER_LITERAL, "42"),
            tok(TokenType.EOF)
        )

        val result = rule.parse(tokens, 0)
        assertTrue(result is ParseResult.Failure)
    }

    // ---------- Opt ----------
    @Test
    fun `Opt present`() {
        val rule = opt(token(TokenType.EQUAL))
        val tokens = listOf(tok(TokenType.EQUAL, "="), tok(TokenType.EOF))

        val result = rule.parse(tokens, 0) as ParseResult.Success
        assertNotNull(result.value)
        assertEquals(1, result.next)
    }

    @Test
    fun `Opt absent returns null and does not advance`() {
        val rule = opt(token(TokenType.EQUAL))
        val tokens = listOf(tok(TokenType.SEMICOLON, ";"), tok(TokenType.EOF))

        val result = rule.parse(tokens, 0) as ParseResult.Success
        assertNull(result.value)
        assertEquals(0, result.next) // no avanzó
    }

    // ---------- Choice ----------
    @Test
    fun `Choice takes first success`() {
        val rule = choice(
            token(TokenType.LET),
            token(TokenType.IDENTIFIER)
        )
        val tokens = listOf(tok(TokenType.IDENTIFIER, "x"), tok(TokenType.EOF))

        val result = rule.parse(tokens, 0) as ParseResult.Success
        assertEquals("x", (result.value as recurses.Token).value)
    }

    @Test
    fun `Choice all fail`() {
        val rule = choice(
            token(TokenType.LET),
            token(TokenType.SEMICOLON)      // ← uno que sí tengas
        )
        val tokens = listOf(
            tok(TokenType.IDENTIFIER, "x"),
            tok(TokenType.EOF)
        )

        val result = rule.parse(tokens, 0)
        assertTrue(result is ParseResult.Failure)
    }

    // ---------- Action ----------
    @Test
    fun `Action transforms value`() {
        val rule = action(token(TokenType.NUMBER_LITERAL)) { value ->
            (value as recurses.Token).value.toDouble()
        }
        val tokens = listOf(tok(TokenType.NUMBER_LITERAL, "42"), tok(TokenType.EOF))

        val result = rule.parse(tokens, 0) as ParseResult.Success
        assertEquals(42.0, result.value)
    }
}