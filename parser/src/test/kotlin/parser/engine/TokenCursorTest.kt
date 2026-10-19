package parser.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import parser.tok
import token.TokenType

class TokenCursorTest {
    @Test
    fun `test ListTokenCursor navigation and mark reset release`() {
        val tokens =
            listOf(
                tok(TokenType.LET, "let"),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.SEMICOLON, ";"),
            )
        val cursor = ListTokenCursor(tokens)

        assertEquals("let", cursor.peek()?.value)
        assertEquals("x", cursor.peek(1)?.value)
        assertEquals(0, cursor.position())

        val mark = cursor.mark()
        cursor.advance(2)
        assertEquals(2, cursor.position())
        assertEquals(";", cursor.peek()?.value)

        cursor.reset(mark)
        assertEquals(0, cursor.position())
        assertEquals("let", cursor.peek()?.value)

        cursor.release(mark)
        cursor.advance(3)
        assertNull(cursor.peek())
    }

    @Test
    fun `test BufferedTokenCursor consumes from iterator with compaction`() {
        val tokens =
            listOf(
                tok(TokenType.LET, "let"),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.EQUAL, "="),
                tok(TokenType.NUMBER_LITERAL, "1"),
                tok(TokenType.SEMICOLON, ";"),
            )
        val cursor = BufferedTokenCursor(tokens.iterator(), initialCapacity = 2)

        assertEquals("let", cursor.peek()?.value)
        assertEquals("x", cursor.peek(1)?.value)
        assertEquals(0, cursor.position())

        val mark = cursor.mark()
        cursor.advance(2)
        assertEquals(2, cursor.position())
        assertEquals("=", cursor.peek()?.value)

        cursor.reset(mark)
        assertEquals(0, cursor.position())
        assertEquals("let", cursor.peek()?.value)
        cursor.release(mark)

        cursor.advance(2)
        assertEquals("=", cursor.peek()?.value)
        cursor.advance(3)
        assertNull(cursor.peek())
    }
}
