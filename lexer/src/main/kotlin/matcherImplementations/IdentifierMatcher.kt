package matcherImplementations

import LexerCursor
import TokenMatcher
import recurses.Position
import recurses.TokenType
import result.LexicalError
import recurses.Token
import result.Result

class IdentifierMatcher(
    private val keywords: Map<String, TokenType>, // --> Recibe let, const, etc
) : TokenMatcher {
    override val name = "identifier"

    override fun canStartWith(character: Char): Boolean =
        character.isLetter() || character == '_' // --> Solo letras o lower_case

    override fun match(
        cursor: LexerCursor,
        start: Position,
    ): Result<Token, LexicalError> {
        val text = buildString {
            while (true) {
                val next = cursor.peek() ?: break // --> Queda caracter por leer?

                if (!next.isLetterOrDigit() && next != '_') {
                    break
                }

                append(cursor.read()!!)
            }
        }

        val type = keywords[text] ?: TokenType.IDENTIFIER // --> si se encuentra definido en los keywords
        // se usa su tipo, si no un identifier

        return Result.Success(
            Token(
                type = type,
                value = text,
                start = start,
                end = cursor.position,
            ),
        )
    }
}
