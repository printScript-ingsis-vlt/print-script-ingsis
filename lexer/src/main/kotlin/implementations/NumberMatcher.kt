package implementations

import LexerCursor
import TokenMatcher
import recurses.Position
import recurses.Token
import recurses.TokenType
import result.LexicalError
import result.Result

class NumberMatcher : TokenMatcher {
    override val name = "number"

    override fun canStartWith(character: Char): Boolean = character.isDigit() // --> Solo digitos aceptados

    override fun match(
        cursor: LexerCursor,
        start: Position,
    ): Result<Token, LexicalError> {
        val text = StringBuilder()

        while (cursor.peek()?.isDigit() == true) {
            text.append(cursor.read())
        }

        // Después de un punto se requiere al menos un dígito.
        if (cursor.peek() == '.') {
            val dotPosition = cursor.position
            text.append(cursor.read())

            if (cursor.peek()?.isDigit() != true) {
                return Result.Failure(
                    LexicalError(
                        dotPosition,
                        "Se esperaba un dígito después del punto decimal",
                    ),
                )
            }

            while (cursor.peek()?.isDigit() == true) {
                text.append(cursor.read())
            }
        }

        return Result.Success(
            Token(
                type = TokenType.NUMBER_LITERAL,
                value = text.toString(),
                start = start,
                end = cursor.position,
            ),
        )
    }
}
