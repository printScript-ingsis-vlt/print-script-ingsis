package token

import ast.Position

data class Token(
    val type: TokenType,
    val value: String,
    val start: Position,
    val end: Position,
)
