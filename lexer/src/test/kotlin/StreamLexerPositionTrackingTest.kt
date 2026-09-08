import help.LexerTestHelper.assertToken
import help.LexerTestHelper.successfulToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import recurses.TokenType

class StreamLexerPositionTrackingTest {
    @Test
    fun `tracks tokens across whitespace and multiple lines`() {
        val lexer = StreamLexer.fromString(" \tlet name\n  42")

        assertToken(successfulToken(lexer.nextToken()), TokenType.LET, "let", 1, 3)
        assertToken(successfulToken(lexer.nextToken()), TokenType.IDENTIFIER, "name", 1, 7)
        assertToken(successfulToken(lexer.nextToken()), TokenType.NUMBER_LITERAL, "42", 2, 3)

        val eof = successfulToken(lexer.nextToken())
        assertEquals(TokenType.EOF, eof.type)
        assertEquals(2, eof.start.line)
        assertEquals(5, eof.start.column)
    }
}
