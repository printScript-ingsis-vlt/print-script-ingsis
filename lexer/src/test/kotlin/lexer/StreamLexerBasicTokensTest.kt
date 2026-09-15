package lexer

import lexer.help.LexerTestHelper.assertToken
import lexer.help.LexerTestHelper.successfulToken
import org.junit.jupiter.api.Test
import token.TokenType

class StreamLexerBasicTokensTest {
    @Test
    fun `recognizes every default single character token`() {
        val lexer = StreamLexer.fromString(": = ; + - * / ( )")
        val expectedTokens =
            listOf(
                TokenType.COLON to ":",
                TokenType.EQUAL to "=",
                TokenType.SEMICOLON to ";",
                TokenType.PLUS to "+",
                TokenType.MINUS to "-",
                TokenType.STAR to "*",
                TokenType.SLASH to "/",
                TokenType.LEFT_PAREN to "(",
                TokenType.RIGHT_PAREN to ")",
            )

        expectedTokens.forEachIndexed { index, (type, value) ->
            assertToken(successfulToken(lexer.nextToken()), type, value, 1, index * 2 + 1)
        }
    }

    @Test
    fun `recognizes keywords and identifiers with underscores and digits`() {
        val lexer = StreamLexer.fromString("let variable_2 letx _private")

        assertToken(successfulToken(lexer.nextToken()), TokenType.LET, "let", 1, 1)
        assertToken(successfulToken(lexer.nextToken()), TokenType.IDENTIFIER, "variable_2", 1, 5)
        assertToken(successfulToken(lexer.nextToken()), TokenType.IDENTIFIER, "letx", 1, 16)
        assertToken(successfulToken(lexer.nextToken()), TokenType.IDENTIFIER, "_private", 1, 21)
    }
}
