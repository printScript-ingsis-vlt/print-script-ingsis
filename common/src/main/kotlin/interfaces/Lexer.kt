package interfaces
import recurses.Token
import result.LexicalError
import result.Result

interface Lexer {
    fun hasNext(): Boolean

    fun nextToken(): Result<Token, LexicalError>

    fun tokenize(): Result<List<Token>, LexicalError>
}
