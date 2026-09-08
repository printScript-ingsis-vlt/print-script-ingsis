package help

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import recurses.Token
import recurses.TokenType
import result.LexicalError
import result.Result

object LexerTestHelper {
    fun assertToken(
        token: Token,
        expectedType: TokenType,
        expectedValue: String,
        expectedStartLine: Int,
        expectedStartCol: Int,
    ) {
        assertEquals(expectedType, token.type)
        assertEquals(expectedValue, token.value)
        assertEquals(expectedStartLine, token.start.line)
        assertEquals(expectedStartCol, token.start.column)
    }

    fun successfulToken(result: Result<Token, LexicalError>): Token =
        when (result) {
            is Result.Success -> result.value
            is Result.Failure -> fail("Expected a token but received: ${result.error.message}")
        }

    fun lexicalError(result: Result<Token, LexicalError>): LexicalError =
        when (result) {
            is Result.Success -> fail("Expected a lexical error but received: ${result.value}")
            is Result.Failure -> result.error
        }
}
