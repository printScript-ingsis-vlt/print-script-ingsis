package parser.grammar

import ast.BooleanLiteral
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.engine.ParseResult
import parser.tok
import token.TokenType.BOOLEAN
import token.TokenType.COLON
import token.TokenType.EOF
import token.TokenType.EQUAL
import token.TokenType.IDENTIFIER
import token.TokenType.LET
import token.TokenType.SEMICOLON
import token.TokenType.TRUE

class GrammarDeclarationV11Test {
    private val expressionV11 = ExpressionRule(extraPrimaries = listOf(BooleanLiteralRule.rule)).expression
    private val declarationV11 =
        DeclarationRule(expressionV11, extraTypeTokens = listOf(Terminals.BOOLEAN)).rule
    private val declarationV10 = DeclarationRule(ExpressionRule().expression).rule

    @Test
    fun `v1_1 parses a boolean declaration`() {
        // let flag: boolean = true;
        val tokens =
            listOf(
                tok(LET, "let"),
                tok(IDENTIFIER, "flag"),
                tok(COLON, ":"),
                tok(BOOLEAN, "boolean"),
                tok(EQUAL, "="),
                tok(TRUE, "true"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = declarationV11.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val stmt = (result as ParseResult.Success).value as VariableDeclaration
        assertEquals("flag", stmt.name)
        assertEquals("boolean", stmt.type)
        assertTrue(stmt.value is BooleanLiteral)
        assertEquals(true, (stmt.value as BooleanLiteral).value)
    }

    @Test
    fun `v1_0 fails on a boolean type token`() {
        // v1.0 no acepta el token BOOLEAN en la posicion de tipo, solo IDENTIFIER
        val tokens =
            listOf(
                tok(LET, "let"),
                tok(IDENTIFIER, "flag"),
                tok(COLON, ":"),
                tok(BOOLEAN, "boolean"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = declarationV10.parse(tokens, 0)

        assertTrue(result is ParseResult.Failure)
    }
}
