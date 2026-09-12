package lexer.implementations

import ast.Position
import lexer.LexerCursor
import lexer.TokenMatcher
import result.LexicalError
import result.Result
import token.Token
import token.TokenType

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
        var result: Result<Token, LexicalError>? = null

        while (result == null) {
            val character = cursor.read()
            result =
                when {
                    character == null ->
                        // Se alcanzó EOF sin comillas de cierre.
                        Result.Failure(
                            LexicalError(start, "String sin cerrar"),
                        )

                    character == quote ->
                        Result.Success(
                            Token(
                                type = TokenType.STRING_LITERAL,
                                value = text.toString(),
                                start = start,
                                end = cursor.position,
                            ),
                        )

                    character == '\n' ->
                        // El string no puede atravesar un salto de línea.
                        Result.Failure(
                            LexicalError(
                                start,
                                "String sin cerrar antes de fin de línea",
                            ),
                        )

                    else -> {
                        text.append(character)
                        null
                    }
                }
        }

        return result
    }
}
