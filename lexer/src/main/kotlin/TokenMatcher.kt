import recurses.Position
import recurses.Token
import result.LexicalError
import result.Result

interface TokenMatcher {
    val name: String

    fun canStartWith(character: Char): Boolean // --> Responde al lexer si puede leer ese caracter

    fun match( // --> Consume todo lo perteneciente al token
        cursor: LexerCursor,
        start: Position,
    ): Result<Token, LexicalError>
}
