package parser.grammar

import ast.BinaryExpression
import ast.Identifier
import ast.NumberLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.engine.ParseResult
import parser.tok
import token.TokenType.EOF
import token.TokenType.IDENTIFIER
import token.TokenType.LEFT_PAREN
import token.TokenType.MINUS
import token.TokenType.NUMBER_LITERAL
import token.TokenType.PLUS
import token.TokenType.RIGHT_PAREN
import token.TokenType.STAR

class GrammarExpressionTest {
    @Test
    fun `single literal`() {
        val tokens = listOf(tok(NUMBER_LITERAL, "5"), tok(EOF))

        val result = ExpressionRule.expression.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val expr = (result as ParseResult.Success).value as NumberLiteral
        assertEquals(5.0, expr.value)
    }

    @Test
    fun `multiplication binds tighter than addition`() {
        // 2 + 3 * 4  ->  2 + (3 * 4)
        val tokens =
            listOf(
                tok(NUMBER_LITERAL, "2"),
                tok(PLUS, "+"),
                tok(NUMBER_LITERAL, "3"),
                tok(STAR, "*"),
                tok(NUMBER_LITERAL, "4"),
                tok(EOF),
            )

        val result = ExpressionRule.expression.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val expr = (result as ParseResult.Success).value as BinaryExpression
        assertEquals("+", expr.operator)
        assertEquals(2.0, (expr.left as NumberLiteral).value)

        val right = expr.right as BinaryExpression
        assertEquals("*", right.operator)
        assertEquals(3.0, (right.left as NumberLiteral).value)
        assertEquals(4.0, (right.right as NumberLiteral).value)
    }

    @Test
    fun `same precedence is left associative`() {
        // 10 - 2 - 3  ->  (10 - 2) - 3
        val tokens =
            listOf(
                tok(NUMBER_LITERAL, "10"),
                tok(MINUS, "-"),
                tok(NUMBER_LITERAL, "2"),
                tok(MINUS, "-"),
                tok(NUMBER_LITERAL, "3"),
                tok(EOF),
            )

        val result = ExpressionRule.expression.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val expr = (result as ParseResult.Success).value as BinaryExpression
        assertEquals("-", expr.operator)
        assertEquals(3.0, (expr.right as NumberLiteral).value)

        val left = expr.left as BinaryExpression
        assertEquals("-", left.operator)
        assertEquals(10.0, (left.left as NumberLiteral).value)
        assertEquals(2.0, (left.right as NumberLiteral).value)
    }

    @Test
    fun `parentheses override precedence`() {
        // (2 + 3) * 4
        val tokens =
            listOf(
                tok(LEFT_PAREN, "("),
                tok(NUMBER_LITERAL, "2"),
                tok(PLUS, "+"),
                tok(NUMBER_LITERAL, "3"),
                tok(RIGHT_PAREN, ")"),
                tok(STAR, "*"),
                tok(NUMBER_LITERAL, "4"),
                tok(EOF),
            )

        val result = ExpressionRule.expression.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        val expr = (result as ParseResult.Success).value as BinaryExpression
        assertEquals("*", expr.operator)

        val left = expr.left as BinaryExpression
        assertEquals("+", left.operator)
        assertEquals(2.0, (left.left as NumberLiteral).value)
        assertEquals(3.0, (left.right as NumberLiteral).value)
        assertEquals(4.0, (expr.right as NumberLiteral).value)
    }

    @Test
    fun `identifier alone is still a valid expression`() {
        val tokens = listOf(tok(IDENTIFIER, "x"), tok(EOF))

        val result = ExpressionRule.expression.parse(tokens, 0)

        assertTrue(result is ParseResult.Success)
        assertEquals("x", ((result as ParseResult.Success).value as Identifier).name)
    }
}
