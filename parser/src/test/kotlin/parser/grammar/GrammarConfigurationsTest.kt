package parser.grammar

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.engine.ParseResult
import parser.tok
import token.TokenType.COLON
import token.TokenType.EOF
import token.TokenType.EQUAL
import token.TokenType.IDENTIFIER
import token.TokenType.LEFT_PAREN
import token.TokenType.LET
import token.TokenType.NUMBER_LITERAL
import token.TokenType.RIGHT_PAREN
import token.TokenType.SEMICOLON

class GrammarConfigurationsTest {
    @Test
    fun `v1_0 trae declaration, assignment y print`() {
        assertEquals(3, GrammarConfigurations.v1_0.statementRules.size)
    }

    @Test
    fun `v1_0 default es v1_0`() {
        assertTrue(GrammarConfigurations.default === GrammarConfigurations.v1_0)
    }

    @Test
    fun `cada regla de v1_0 parsea su construccion`() {
        val rules = GrammarConfigurations.v1_0.statementRules.map { it.rule }

        val declarationTokens =
            listOf(
                tok(LET, "let"),
                tok(IDENTIFIER, "x"),
                tok(COLON, ":"),
                tok(IDENTIFIER, "number"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )
        val assignmentTokens =
            listOf(tok(IDENTIFIER, "x"), tok(EQUAL, "="), tok(NUMBER_LITERAL, "5"), tok(SEMICOLON, ";"), tok(EOF))
        val printTokens =
            listOf(
                tok(IDENTIFIER, "println"),
                tok(LEFT_PAREN, "("),
                tok(IDENTIFIER, "x"),
                tok(RIGHT_PAREN, ")"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        assertTrue(rules.any { it.parse(declarationTokens, 0) is ParseResult.Success })
        assertTrue(rules.any { it.parse(assignmentTokens, 0) is ParseResult.Success })
        assertTrue(rules.any { it.parse(printTokens, 0) is ParseResult.Success })
    }
}
