package lexer.implementations

import ast.Position
import lexer.LexerCursor
import lexer.TokenMatcher
import result.LexicalError
import result.Result
import token.Token
import token.TokenType

class IdentifierMatcher(
    // Recibe las keywords configuradas, como let o const.
    private val keywords: Map<String, TokenType>,
) : TokenMatcher {
    override val name = "identifier"

    override fun canStartWith(character: Char): Boolean =
        character.isLetter() || character == '_' // --> Solo letras o lower_case

    override fun match(
        cursor: LexerCursor,
        start: Position,
    ): Result<Token, LexicalError> {
        val text =
            buildString {
                var next = cursor.peek()
                while (next?.let { it.isLetterOrDigit() || it == '_' } == true) {
                    append(cursor.read()!!) // --> !! es porque se sabe que no va a ser nulo
                    next = cursor.peek()
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
