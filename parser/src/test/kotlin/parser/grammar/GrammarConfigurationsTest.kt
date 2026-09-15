package parser.grammar

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.ConfigurableParser
import parser.engine.ParseResult
import parser.tok
import result.Result
import token.TokenType.BOOLEAN
import token.TokenType.COLON
import token.TokenType.CONST
import token.TokenType.ELSE
import token.TokenType.EOF
import token.TokenType.EQUAL
import token.TokenType.IDENTIFIER
import token.TokenType.IF
import token.TokenType.LEFT_BRACE
import token.TokenType.LEFT_PAREN
import token.TokenType.LET
import token.TokenType.NUMBER_LITERAL
import token.TokenType.READENV
import token.TokenType.READINPUT
import token.TokenType.RIGHT_BRACE
import token.TokenType.RIGHT_PAREN
import token.TokenType.SEMICOLON
import token.TokenType.STRING_LITERAL
import token.TokenType.TRUE

class GrammarConfigurationsTest {
    @Test
    fun `v1_0 trae declaration, assignment y print`() {
        assertEquals(3, GrammarConfigurations.v1_0.statementRules.size)
    }

    @Test
    fun `v1_1 default es v1_1`() {
        assertTrue(GrammarConfigurations.default === GrammarConfigurations.v1_1)
    }

    @Test
    fun `v1_1 trae declaration, const, assignment, print e if con soporte boolean`() {
        assertEquals(5, GrammarConfigurations.v1_1.statementRules.size)
    }

    @Test
    fun `v1_1 falla al parsear un else if de punta a punta`() {
        // if (x) { } else if (y) { }  -> no soportado, debe fallar en el parser completo
        val cliParser = ConfigurableParser(GrammarConfigurations.v1_1)
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

        val result = cliParser.parse(tokens)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `v1_1 parsea una declaracion boolean con literal`() {
        val rules = GrammarConfigurations.v1_1.statementRules.map { it.rule }

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

        assertTrue(rules.any { it.parse(tokens, 0) is ParseResult.Success })
    }

    @Test
    fun `v1_0 no reconoce el token boolean como tipo`() {
        val rules = GrammarConfigurations.v1_0.statementRules.map { it.rule }

        val tokens =
            listOf(
                tok(LET, "let"),
                tok(IDENTIFIER, "flag"),
                tok(COLON, ":"),
                tok(BOOLEAN, "boolean"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        assertTrue(rules.none { it.parse(tokens, 0) is ParseResult.Success })
    }

    @Test
    fun `v1_1 parsea readInput en contexto de asignacion`() {
        // x = readInput("Enter value: ");
        val cliParser = ConfigurableParser(GrammarConfigurations.v1_1)
        val tokens =
            listOf(
                tok(IDENTIFIER, "x"),
                tok(EQUAL, "="),
                tok(READINPUT, "readInput"),
                tok(LEFT_PAREN, "("),
                tok(STRING_LITERAL, "Enter value: "),
                tok(RIGHT_PAREN, ")"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = cliParser.parse(tokens)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `v1_1 parsea readInput anidado con readEnv como argumento`() {
        // readInput(readEnv("PROMPT_VAR"))
        val rules = GrammarConfigurations.v1_1.statementRules.map { it.rule }
        val tokens =
            listOf(
                tok(IDENTIFIER, "x"),
                tok(EQUAL, "="),
                tok(READINPUT, "readInput"),
                tok(LEFT_PAREN, "("),
                tok(READENV, "readEnv"),
                tok(LEFT_PAREN, "("),
                tok(STRING_LITERAL, "PROMPT_VAR"),
                tok(RIGHT_PAREN, ")"),
                tok(RIGHT_PAREN, ")"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        assertTrue(rules.any { it.parse(tokens, 0) is ParseResult.Success })
    }

    @Test
    fun `v1_0 no reconoce readInput`() {
        val rules = GrammarConfigurations.v1_0.statementRules.map { it.rule }
        val tokens =
            listOf(
                tok(IDENTIFIER, "x"),
                tok(EQUAL, "="),
                tok(READINPUT, "readInput"),
                tok(LEFT_PAREN, "("),
                tok(STRING_LITERAL, "Enter value: "),
                tok(RIGHT_PAREN, ")"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        assertTrue(rules.none { it.parse(tokens, 0) is ParseResult.Success })
    }

    @Test
    fun `v1_1 parsea una const declaration con inicializador`() {
        val cliParser = ConfigurableParser(GrammarConfigurations.v1_1)
        val tokens =
            listOf(
                tok(CONST, "const"),
                tok(IDENTIFIER, "x"),
                tok(COLON, ":"),
                tok(IDENTIFIER, "Number"),
                tok(EQUAL, "="),
                tok(NUMBER_LITERAL, "42"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        val result = cliParser.parse(tokens)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `v1_0 no reconoce const`() {
        val rules = GrammarConfigurations.v1_0.statementRules.map { it.rule }
        val tokens =
            listOf(
                tok(CONST, "const"),
                tok(IDENTIFIER, "x"),
                tok(COLON, ":"),
                tok(IDENTIFIER, "Number"),
                tok(EQUAL, "="),
                tok(NUMBER_LITERAL, "42"),
                tok(SEMICOLON, ";"),
                tok(EOF),
            )

        assertTrue(rules.none { it.parse(tokens, 0) is ParseResult.Success })
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
