package parser.stream

import ast.Position
import parser.engine.TokenCursor
import result.SyntaxError
import token.TokenType


/** Terminal por TokenType */
data class TokenRule(
    val type: TokenType,
    val expected: String = type.name,
) : StreamRule {
    override fun parse(cursor: TokenCursor): StreamParseResult {
        val token = cursor.peek()
            ?: return StreamParseResult.Failure(
                SyntaxError(Position(0, 0), "Unexpected end of input, expected $expected"),
            )

        return if (token.type == type) {
            cursor.advance()
            StreamParseResult.Success(token)
        } else {
            StreamParseResult.Failure(
                SyntaxError(token.start, "Expected $expected, got '${token.value}'"),
            )
        }
    }
}

/** Terminal por TokenType + valor exacto */
data class LiteralRule(
    val type: TokenType,
    val value: String,
) : StreamRule {
    override fun parse(cursor: TokenCursor): StreamParseResult {
        val token = cursor.peek()
            ?: return StreamParseResult.Failure(
                SyntaxError(Position(0, 0), "Unexpected end of input, expected '$value'"),
            )

        return if (token.type == type && token.value == value) {
            cursor.advance()
            StreamParseResult.Success(token)
        } else {
            StreamParseResult.Failure(
                SyntaxError(token.start, "Expected '$value', got '${token.value}'"),
            )
        }
    }
}
