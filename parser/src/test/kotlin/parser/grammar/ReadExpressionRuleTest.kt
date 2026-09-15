package parser.grammar

import ast.ReadEnvExpression
import ast.ReadInputExpression
import ast.StringLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.engine.ParseResult
import parser.tok
import token.TokenType.EOF
import token.TokenType.LEFT_PAREN
import token.TokenType.READENV
import token.TokenType.READINPUT
import token.TokenType.RIGHT_PAREN
import token.TokenType.STRING_LITERAL

class ReadExpressionRuleTest {
    private val expression = ExpressionRule().expression

    @Test
    fun `parses readInput with a string literal argument`() {
        // readInput("Enter value: ")
        val tokens =
            listOf(
                tok(READINPUT, "readInput"),
                tok(LEFT_PAREN, "("),
                tok(STRING_LITERAL, "Enter value: "),
                tok(RIGHT_PAREN, ")"),
                tok(EOF),
            )

        val result = ReadInputExpressionRule(expression).rule.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val expr = (result as ParseResult.Success).value as ReadInputExpression
        assertTrue(expr.prompt is StringLiteral)
        assertEquals("Enter value: ", (expr.prompt as StringLiteral).value)
    }

    @Test
    fun `parses readEnv with a string literal argument`() {
        // readEnv("HOME")
        val tokens =
            listOf(
                tok(READENV, "readEnv"),
                tok(LEFT_PAREN, "("),
                tok(STRING_LITERAL, "HOME"),
                tok(RIGHT_PAREN, ")"),
                tok(EOF),
            )

        val result = ReadEnvExpressionRule(expression).rule.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val expr = (result as ParseResult.Success).value as ReadEnvExpression
        assertTrue(expr.envVariableName is StringLiteral)
        assertEquals("HOME", (expr.envVariableName as StringLiteral).value)
    }

    @Test
    fun `fails without parentheses`() {
        val tokens = listOf(tok(READINPUT, "readInput"), tok(STRING_LITERAL, "x"), tok(EOF))

        val result = ReadInputExpressionRule(expression).rule.parse(tokens, 0)
        assertTrue(result is ParseResult.Failure)
    }
}
