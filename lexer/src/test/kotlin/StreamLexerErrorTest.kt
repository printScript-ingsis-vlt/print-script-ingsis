import help.LexerTestHelper.lexicalError
import help.LexerTestHelper.successfulToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import recurses.TokenType
import result.Result
import java.io.StringReader

class StreamLexerErrorTest {
    @Test
    fun `reports unexpected characters at their position`() {
        val error = lexicalError(StreamLexer.fromString("@").nextToken())

        assertEquals("Caracter inesperado '@'", error.message)
        assertEquals(1, error.position.line)
        assertEquals(1, error.position.column)
    }

    @Test
    fun `reports strings that end at eof without closing quote`() {
        val error = lexicalError(StreamLexer.fromString("\"hello").nextToken())

        assertEquals("String sin cerrar", error.message)
        assertEquals(1, error.position.line)
        assertEquals(1, error.position.column)
    }

    @Test
    fun `reports strings that reach a new line without closing quote`() {
        val error = lexicalError(StreamLexer.fromString("'hello\n").nextToken())

        assertEquals("String sin cerrar antes de fin de línea", error.message)
    }

    @Test
    fun `tokenize reader stops at the first lexical error`() {
        val result = StreamLexer.tokenize(StringReader("let @"))

        val error = (result as Result.Failure).error

        assertEquals("Caracter inesperado '@'", error.message)
    }

    @Test
    fun `a second decimal point is reported as an unexpected character`() {
        val lexer = StreamLexer.fromString("1.2.3")

        assertEquals(TokenType.NUMBER_LITERAL, successfulToken(lexer.nextToken()).type)
        assertEquals("Caracter inesperado '.'", lexicalError(lexer.nextToken()).message)
    }
}
