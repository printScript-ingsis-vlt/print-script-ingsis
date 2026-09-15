package parser.grammar

import ast.Identifier
import ast.IfStatement
import ast.PrintStatement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.engine.Choice
import parser.engine.ParseResult
import parser.engine.Rule
import parser.engine.ref
import parser.tok
import token.TokenType.ELSE
import token.TokenType.EOF
import token.TokenType.IDENTIFIER
import token.TokenType.IF
import token.TokenType.LEFT_BRACE
import token.TokenType.LEFT_PAREN
import token.TokenType.RIGHT_BRACE
import token.TokenType.RIGHT_PAREN
import token.TokenType.SEMICOLON

class IfStatementRuleTest {
    private val expression = ExpressionRule().expression

    // simula el dispatcher de statements de la version: declaracion (no usada aca),
    // asignacion, print, y el if mismo para poder anidar
    private lateinit var statement: Rule
    private val printStatement = PrintStatementRule(expression)
    private val ifStatement: Rule by lazy { IfStatementRule(expression, statement).rule }

    init {
        statement = Choice(listOf(printStatement.rule, ref { ifStatement }))
    }

    @Test
    fun `parses an if with a single statement block`() {
        // if (x) { println(x); }
        val tokens =
            listOf(
                tok(IF, "if"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
                tok(LEFT_BRACE, "{"),
                tok(IDENTIFIER, "println"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
                tok(SEMICOLON, ";"),
                tok(RIGHT_BRACE, "}"),
                tok(EOF),
            )

        val result = ifStatement.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val stmt = (result as ParseResult.Success).value as IfStatement
        assertTrue(stmt.condition is Identifier)
        assertEquals(1, stmt.thenBranch.size)
        assertTrue(stmt.thenBranch[0] is PrintStatement)
        assertNull(stmt.elseBranch)
    }

    @Test
    fun `parses if with else`() {
        // if (x) { println(x); } else { println(x); }
        val thenElseTokens =
            listOf(
                tok(LEFT_BRACE, "{"),
                tok(IDENTIFIER, "println"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
                tok(SEMICOLON, ";"),
                tok(RIGHT_BRACE, "}"),
            )
        val tokens =
            listOf(
                tok(IF, "if"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
            ) + thenElseTokens + listOf(tok(ELSE, "else")) + thenElseTokens + listOf(tok(EOF))

        val result = ifStatement.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val stmt = (result as ParseResult.Success).value as IfStatement
        assertEquals(1, stmt.thenBranch.size)
        assertEquals(1, stmt.elseBranch?.size)
    }

    @Test
    fun `parses an empty block`() {
        // if (x) { }
        val tokens =
            listOf(
                tok(IF, "if"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
                tok(LEFT_BRACE, "{"),
                tok(RIGHT_BRACE, "}"),
                tok(EOF),
            )

        val result = ifStatement.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        assertTrue(((result as ParseResult.Success).value as IfStatement).thenBranch.isEmpty())
    }

    @Test
    fun `fails without braces around the then block`() {
        // if (x) println(x);  -> sin { }
        val tokens =
            listOf(
                tok(IF, "if"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
                tok(IDENTIFIER, "println"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = ifStatement.parse(tokens, 0)
        assertTrue(result is ParseResult.Failure)
    }

    @Test
    fun `does not attach an else if to the then block`() {
        // if (x) { } else if (y) { }
        // La regla en aislado no falla: "opt" descarta el else y devuelve exito
        // parseando solo "if (x) { }", sin consumir "else if (y) { }". Ese resto sin
        // consumir es lo que despues hace fallar al ConfigurableParser completo
        // (ver GrammarConfigurationsTest."v1_1 falla al parsear un else if de punta a punta").
        val elseIndex = 6
        val tokens =
            listOf(
                tok(IF, "if"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
                tok(LEFT_BRACE, "{"),
                tok(RIGHT_BRACE, "}"),
                tok(ELSE, "else"),
                tok(IF, "if"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "y"),
                tok(RIGHT_PAREN, ")"),
                tok(LEFT_BRACE, "{"),
                tok(RIGHT_BRACE, "}"),
                tok(EOF),
            )

        val result = ifStatement.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        result as ParseResult.Success
        assertNull((result.value as IfStatement).elseBranch)
        assertEquals(elseIndex, result.next)
    }

    @Test
    fun `parses a nested if inside the then block`() {
        // if (x) { if (y) { } }
        val tokens =
            listOf(
                tok(IF, "if"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
                tok(LEFT_BRACE, "{"),
                tok(IF, "if"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "y"),
                tok(RIGHT_PAREN, ")"),
                tok(LEFT_BRACE, "{"),
                tok(RIGHT_BRACE, "}"),
                tok(RIGHT_BRACE, "}"),
                tok(EOF),
            )

        val result = ifStatement.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val outer = (result as ParseResult.Success).value as IfStatement
        assertEquals(1, outer.thenBranch.size)
        assertTrue(outer.thenBranch[0] is IfStatement)
    }
}
