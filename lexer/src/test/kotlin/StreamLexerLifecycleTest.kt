import help.LexerTestHelper.successfulToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import recurses.TokenType
import result.Result

class StreamLexerLifecycleTest {
    @Test
    fun `emitting eof ends the lexer and prohibits further reads`() {
        val lexer = StreamLexer.fromString("")

        assertTrue(lexer.hasNext())
        assertEquals(TokenType.EOF, successfulToken(lexer.nextToken()).type)
        assertFalse(lexer.hasNext())
        assertThrows(NoSuchElementException::class.java) { lexer.nextToken() }
    }

    @Test
    fun `instance tokenize consumes the stream and returns eof`() {
        val result = StreamLexer.fromString("let").tokenize()

        val tokens = (result as Result.Success).value

        assertEquals(listOf(TokenType.LET, TokenType.EOF), tokens.map { it.type })
    }
}
