package lexer

import lexer.help.LexerTestHelper.assertToken
import lexer.help.LexerTestHelper.successfulToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import result.Result
import token.TokenType

class StreamLexerLiteralTest {
    @Test
    fun `recognizes integer and decimal numbers`() {
        val lexer = StreamLexer.fromString("42 3.14")

        assertToken(successfulToken(lexer.nextToken()), TokenType.NUMBER_LITERAL, "42", 1, 1)
        assertToken(successfulToken(lexer.nextToken()), TokenType.NUMBER_LITERAL, "3.14", 1, 4)
    }

    @Test
    fun `recognizes single and double quoted strings`() {
        val lexer = StreamLexer.fromString("\"hello\" 'world' \"\"")

        assertToken(successfulToken(lexer.nextToken()), TokenType.STRING_LITERAL, "hello", 1, 1)
        assertToken(successfulToken(lexer.nextToken()), TokenType.STRING_LITERAL, "world", 1, 9)
        assertToken(successfulToken(lexer.nextToken()), TokenType.STRING_LITERAL, "", 1, 17)
    }

    @Test
    fun `tokenize source returns tokens including eof`() {
        val result = StreamLexer.tokenize("let name")

        val tokens = (result as Result.Success).value

        assertEquals(listOf(TokenType.LET, TokenType.IDENTIFIER, TokenType.EOF), tokens.map { it.type })
    }
}
