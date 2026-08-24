package interfaces
import recurses.Token
import result.Result
import result.LexicalError

interface Lexer {
    fun hasNext(): Boolean
    fun nextToken(): Result<Token, LexicalError>
}