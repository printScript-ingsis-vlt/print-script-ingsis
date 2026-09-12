package lexer

import result.LexicalError
import result.Result
import token.Token

interface Lexer {
    fun hasNext(): Boolean

    fun nextToken(): Result<Token, LexicalError>

    fun tokenize(): Result<List<Token>, LexicalError>
}
