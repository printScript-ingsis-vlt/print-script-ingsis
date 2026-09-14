package parser.engine

import ast.Assignment
import ast.BinaryExpression
import ast.PrintStatement
import ast.Program
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.ConfigurableParser
import parser.grammar.DeclarationRule
import parser.grammar.ExpressionRule
import parser.grammar.GrammarConfiguration
import parser.tok
import result.Result
import result.SyntaxError
import token.TokenType

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

    @Test
    fun `parsea una asignacion suelta`() {
        // x = 10;
        val tokens =
            listOf(
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.EQUAL, "="),
                tok(TokenType.NUMBER_LITERAL, "10"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.EOF, ""),
            )

        val result = parser.parse(tokens)

        assertTrue(result is Result.Success<*>)
        val success = result as Result.Success<Program>
        assertEquals(1, success.value.statements.size)
        assertTrue(success.value.statements[0] is Assignment)
    }

    @Test
    fun `parsea println con una expresion con precedencia`() {
        // println(2 + 3 * 4);
        val tokens =
            listOf(
                tok(TokenType.IDENTIFIER, "println"),
                tok(TokenType.LEFT_PAREN, "("),
                tok(TokenType.NUMBER_LITERAL, "2"),
                tok(TokenType.PLUS, "+"),
                tok(TokenType.NUMBER_LITERAL, "3"),
                tok(TokenType.STAR, "*"),
                tok(TokenType.NUMBER_LITERAL, "4"),
                tok(TokenType.RIGHT_PAREN, ")"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.EOF, ""),
            )

        val result = parser.parse(tokens)

        assertTrue(result is Result.Success<*>)
        val success = result as Result.Success<Program>
        assertEquals(1, success.value.statements.size)

        val print = success.value.statements[0] as PrintStatement
        assertTrue(print.argument is BinaryExpression)
        assertEquals("+", (print.argument as BinaryExpression).operator)
    }

    @Test
    fun `parsea declaracion, asignacion y print en el mismo programa`() {
        // let x: number = 5; x = 10; println(x);
        val tokens =
            listOf(
                tok(TokenType.LET, "let"),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.COLON, ":"),
                tok(TokenType.IDENTIFIER, "number"),
                tok(TokenType.EQUAL, "="),
                tok(TokenType.NUMBER_LITERAL, "5"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.EQUAL, "="),
                tok(TokenType.NUMBER_LITERAL, "10"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.IDENTIFIER, "println"),
                tok(TokenType.LEFT_PAREN, "("),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.RIGHT_PAREN, ")"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.EOF, ""),
            )

        val result = parser.parse(tokens)

        assertTrue(result is Result.Success<*>)
        val success = result as Result.Success<Program>
        assertEquals(3, success.value.statements.size)
        assertTrue(success.value.statements[0] is VariableDeclaration)
        assertTrue(success.value.statements[1] is Assignment)
        assertTrue(success.value.statements[2] is PrintStatement)
    }

    @Test
    fun `una configuracion reducida rechaza lo que no esta habilitado`() {
        // solo declaration habilitada: println no debería poder parsearse
        val onlyDeclarations =
            ConfigurableParser(
                GrammarConfiguration(
                    statementRules = listOf(DeclarationRule(ExpressionRule().expression)),
                ),
            )

        val tokens =
            listOf(
                tok(TokenType.IDENTIFIER, "println"),
                tok(TokenType.LEFT_PAREN, "("),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.RIGHT_PAREN, ")"),
                tok(TokenType.SEMICOLON, ";"),
                tok(TokenType.EOF, ""),
            )

        val result = onlyDeclarations.parse(tokens)

        assertTrue(result is Result.Failure<*>)
    }
}
