package grammar

import engine.ParseResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import recurses.Assignment
import recurses.NumberLiteral
import recurses.Identifier
import recurses.VariableDeclaration
import recurses.TokenType.*
import tok

class GrammarDeclarationTest {
    @Test
    fun `assignment with number literal`() {
        // a = 10;
        val tokens =
            listOf(
                tok(IDENTIFIER, "a"),
                tok(EQUAL, "="),
                tok(NUMBER_LITERAL, "10"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = Grammar.assignment.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val stmt = (result as ParseResult.Success).value as Assignment

        assertEquals("a", stmt.name)
        assertTrue(stmt.value is NumberLiteral)
        assertEquals(10.0, (stmt.value as NumberLiteral).value)
        assertEquals(4, result.next)
    }

    @Test
    fun `assignment with identifier`() {
        // a = b;
        val tokens =
            listOf(
                tok(IDENTIFIER, "a"),
                tok(EQUAL, "="),
                tok(IDENTIFIER, "b"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = Grammar.assignment.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val stmt = (result as ParseResult.Success).value as Assignment

        assertEquals("a", stmt.name)
        assertTrue(stmt.value is Identifier)
        assertEquals("b", (stmt.value as Identifier).name)
        assertEquals(4, result.next)
    }

    @Test
    fun `assignment fails when missing equal`() {
        val tokens =
            listOf(
                tok(IDENTIFIER, "a"),
                tok(NUMBER_LITERAL, "10"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = Grammar.assignment.parse(tokens, 0)
        assertTrue(result is ParseResult.Failure)
    }

    @Test
    fun `assignment fails when missing semicolon`() {
        val tokens =
            listOf(
                tok(IDENTIFIER, "a"),
                tok(EQUAL, "="),
                tok(NUMBER_LITERAL, "10"),
                tok(EOF),
            )

        val result = Grammar.assignment.parse(tokens, 0)
        assertTrue(result is ParseResult.Failure)
    }

    @Test
    fun `declaration with initializer`() {
        val tokens =
            listOf(
                tok(LET, "let"),
                tok(IDENTIFIER, "x"),
                tok(COLON, ":"),
                tok(IDENTIFIER, "Number"),
                tok(EQUAL, "="),
                tok(NUMBER_LITERAL, "42"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = Grammar.declaration.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val stmt = (result as ParseResult.Success).value as VariableDeclaration

        assertEquals("x", stmt.name)
        assertEquals("Number", stmt.type)
        assertTrue(stmt.value is NumberLiteral)
        assertEquals(42.0, (stmt.value as NumberLiteral).value)
        assertEquals(7, result.next)
    }

    @Test
    fun `declaration without initializer`() {
        val tokens =
            listOf(
                tok(LET, "let"),
                tok(IDENTIFIER, "y"),
                tok(COLON, ":"),
                tok(IDENTIFIER, "String"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = Grammar.declaration.parse(tokens, 0) as ParseResult.Success
        val stmt = result.value as VariableDeclaration

        assertEquals("y", stmt.name)
        assertEquals("String", stmt.type)
        assertNull(stmt.value)
        assertEquals(5, result.next)
    }

    @Test
    fun `declaration fails when missing colon`() {
        val tokens =
            listOf(
                tok(LET, "let"),
                tok(IDENTIFIER, "x"),
                tok(IDENTIFIER, "Number"), // falta el :
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = Grammar.declaration.parse(tokens, 0)
        assertTrue(result is ParseResult.Failure)
    }

    @Test
    fun `declaration fails when missing semicolon`() {
        val tokens =
            listOf(
                tok(LET, "let"),
                tok(IDENTIFIER, "x"),
                tok(COLON, ":"),
                tok(IDENTIFIER, "Number"),
                tok(EOF),
            )

        val result = Grammar.declaration.parse(tokens, 0)
        assertTrue(result is ParseResult.Failure)
    }
}
