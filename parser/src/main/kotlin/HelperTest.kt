import recurses.Position
import recurses.Token
import recurses.TokenType

fun tok(
    type: TokenType,
    value: String = type.name.lowercase(),
    line: Int = 1,
    column: Int = 1
): Token = Token(type, value, Position(line, column),Position(line, column))