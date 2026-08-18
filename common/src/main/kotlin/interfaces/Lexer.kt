package austral.src.main.kotlin.commun


import common.src.main.kotlin.result.LexicalError
import common.src.main.kotlin.result.Result
import recurses.Token

interface Lexer {
    fun hasNext(): Boolean
    fun nextToken(): Result<Token, LexicalError>
}