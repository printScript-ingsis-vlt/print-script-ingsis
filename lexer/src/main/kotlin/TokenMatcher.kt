import recurses.Position
import recurses.Token
import result.LexicalError
import result.Result

interface TokenMatcher {
    val name: String

    fun canStartWith(character: Char): Boolean // --> Responde al lexer si puede leer ese caracter

    // Consume todos los caracteres pertenecientes al token.
    fun match(
        cursor: LexerCursor,
        start: Position,
    ): Result<Token, LexicalError>
}
