package engine

import ConfigurableParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import recurses.Program
import recurses.TokenType
import recurses.VariableDeclaration
import result.Result
import result.SyntaxError
import tok

class ConfigurableParserTest {
    private val parser = ConfigurableParser()

    @Test
    fun `parsea una declaracion valida`() {
        val tokens =
            listOf(
                tok(TokenType.LET, "let"),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.COLON, ":"),
                tok(TokenType.IDENTIFIER, "number"),
                tok(TokenType.EQUAL, "="),
                tok(TokenType.NUMBER_LITERAL, "5"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.EOF, ""),
            )

        val result = parser.parse(tokens)

        assertTrue(result is Result.Success<*>)

        val success = result as Result.Success<Program>

        assertEquals(1, success.value.statements.size)

        val declaration = success.value.statements[0] as VariableDeclaration
        assertEquals("x", declaration.name)
        assertEquals("number", declaration.type)
    }

    @Test
    fun `parsea multiples declaraciones`() {
        val tokens =
            listOf(
                tok(TokenType.LET, "let"),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.COLON, ":"),
                tok(TokenType.IDENTIFIER, "number"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.LET, "let"),
                tok(TokenType.IDENTIFIER, "y"),
                tok(TokenType.COLON, ":"),
                tok(TokenType.IDENTIFIER, "string"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.EOF, ""),
            )

        val result = parser.parse(tokens)

        assertTrue(result is Result.Success<*>)

        val success = result as Result.Success<Program>

        assertEquals(2, success.value.statements.size)
    }

    @Test
    fun `devuelve failure cuando falta el tipo`() {
        val tokens =
            listOf(
                tok(TokenType.LET, "let"),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.COLON, ":"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.EOF, ""),
            )

        val result = parser.parse(tokens)

        assertTrue(result is Result.Failure<*>)

        val failure = result as Result.Failure<List<SyntaxError>>

        assertEquals(1, failure.error.size)
    }

    @Test
    fun `se sincroniza y sigue parseando luego de un error`() {
        val tokens =
            listOf(
                tok(TokenType.LET, "let"),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.COLON, ":"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.LET, "let"),
                tok(TokenType.IDENTIFIER, "y"),
                tok(TokenType.COLON, ":"),
                tok(TokenType.IDENTIFIER, "number"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.EOF, ""),
            )

        val result = parser.parse(tokens)

        assertTrue(result is Result.Failure<*>)

        val failure = result as Result.Failure<List<SyntaxError>>

        assertEquals(1, failure.error.size)
    }

    @Test
    fun `programa vacio devuelve success`() {
        val result = parser.parse(listOf(tok(TokenType.EOF, "")))

        assertTrue(result is Result.Success<*>)

        val success = result as Result.Success<Program>

        assertEquals(0, success.value.statements.size)
    }
}
