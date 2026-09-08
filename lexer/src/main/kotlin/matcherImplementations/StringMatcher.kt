package matcherImplementations

import LexerCursor
import TokenMatcher
import recurses.Position
import recurses.TokenType
import result.LexicalError
import recurses.Token
import result.Result

class StringMatcher : TokenMatcher {
    override val name = "string"

    override fun canStartWith(character: Char): Boolean =
        character == '"' || character == '\'' // --> Un string puede iniciar solo con " o con /

    override fun match(
        cursor: LexerCursor,
        start: Position,
    ): Result<Token, LexicalError> {
        val quote = cursor.read()!! // --> comillas iniciales
        val text = StringBuilder()

        while (true) {
            val character = cursor.read()
                ?: return Result.Failure( // --> Caso de llegar a un EOF sin " finales
                    LexicalError(start, "String sin cerrar"),
                )

            when {
                character == quote -> { // --> Cuando se llegue a unas comillas finales, termina el matcheo
                    return Result.Success(
                        Token(
                            type = TokenType.STRING_LITERAL,
                            value = text.toString(),
                            start = start,
                            end = cursor.position,
                        ),
                    )
                }

                character == '\n' -> { // --> Salto de linea sin cerrar comillas
                    return Result.Failure(
                        LexicalError(
                            start,
                            "String sin cerrar antes de fin de línea",
                        ),
                    )
                }

                else -> text.append(character)
            }
        }
    }
}
