package austral.src.main.kotlin.commun

import austral.src.main.kotlin.commun.result.LexicalError
import austral.src.main.kotlin.commun.result.Result

interface Lexer {
    fun hasNext(): Boolean
    fun nextToken(): Result<Token, LexicalError>
}