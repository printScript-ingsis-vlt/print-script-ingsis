package lexer

import lexer.help.LexerTestHelper.assertToken
import lexer.help.LexerTestHelper.lexicalError
import lexer.help.LexerTestHelper.successfulToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import token.TokenType

class StreamLexerV11TokensTest {
    @Test
    fun `recognizes keyword const`() {
        assertV11Token("const", TokenType.CONST)
    }

    @Test
    fun `recognizes keyword boolean`() {
        assertV11Token("boolean", TokenType.BOOLEAN)
    }

    @Test
    fun `recognizes keyword if`() {
        assertV11Token("if", TokenType.IF)
    }

    @Test
    fun `recognizes keyword else`() {
        assertV11Token("else", TokenType.ELSE)
    }

    @Test
    fun `recognizes literal true`() {
        assertV11Token("true", TokenType.TRUE)
    }

    @Test
    fun `recognizes literal false`() {
        assertV11Token("false", TokenType.FALSE)
    }

    @Test
    fun `recognizes readInput`() {
        assertV11Token("readInput", TokenType.READINPUT)
    }

    @Test
    fun `recognizes readEnv`() {
        assertV11Token("readEnv", TokenType.READENV)
    }

    @Test
    fun `recognizes left brace`() {
        assertV11Token("{", TokenType.LEFT_BRACE)
    }

    @Test
    fun `recognizes right brace`() {
        assertV11Token("}", TokenType.RIGHT_BRACE)
    }

    @Test
    fun `keeps existing keywords in version 1 1`() {
        assertV11Token("let", TokenType.LET)
    }

    @Test
    fun `does not recognize version 1 1 keywords in version 1 0`() {
        val v11Keywords =
            listOf(
                "const",
                "boolean",
                "if",
                "else",
                "true",
                "false",
                "readInput",
                "readEnv",
            )

        v11Keywords.forEach { keyword ->
            val lexer = StreamLexer.fromString(keyword, LexerConfigurations.v1_0)

            assertToken(
                successfulToken(lexer.nextToken()),
                TokenType.IDENTIFIER,
                keyword,
                1,
                1,
            )
        }
    }

    @Test
    fun `does not recognize braces in version 1 0`() {
        listOf("{", "}").forEach { brace ->
            val error =
                lexicalError(
                    StreamLexer.fromString(brace, LexerConfigurations.v1_0).nextToken(),
                )

            assertEquals("Caracter inesperado '$brace'", error.message)
            assertEquals(1, error.position.line)
            assertEquals(1, error.position.column)
        }
    }

    @Test
    fun `recognizes identifiers that begin with version 1 1 keywords`() {
        val identifiers =
            listOf(
                "constValue",
                "booleanValue",
                "ifElse",
                "trueValue",
                "readInput2",
                "readEnvValue",
            )

        identifiers.forEach { identifier ->
            val lexer = StreamLexer.fromString(identifier, LexerConfigurations.v1_1)

            assertToken(
                successfulToken(lexer.nextToken()),
                TokenType.IDENTIFIER,
                identifier,
                1,
                1,
            )
        }
    }

    private fun assertV11Token(
        source: String,
        expectedType: TokenType,
    ) {
        val lexer = StreamLexer.fromString(source, LexerConfigurations.v1_1)

        assertToken(successfulToken(lexer.nextToken()), expectedType, source, 1, 1)
    }
}
