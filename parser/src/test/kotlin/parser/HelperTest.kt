package parser

import ast.Position
import token.Token
import token.TokenType

fun tok(
    type: TokenType,
    value: String = type.name.lowercase(),
    line: Int = 1,
    column: Int = 1,
): Token = Token(type, value, Position(line, column), Position(line, column))
