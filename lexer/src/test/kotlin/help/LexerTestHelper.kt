package help

import org.junit.jupiter.api.Assertions.assertEquals
import recurses.Token
import recurses.TokenType

object LexerTestHelper {
    fun assertToken(
        token: Token,
        expectedType: TokenType,
        expectedValue: String,
        expectedStartLine: Int,
        expectedStartCol: Int
    ) {
        assertEquals(expectedType, token.type)
        assertEquals(expectedValue, token.value)
        assertEquals(expectedStartLine, token.start.line)
        assertEquals(expectedStartCol, token.start.column)
    }
}
