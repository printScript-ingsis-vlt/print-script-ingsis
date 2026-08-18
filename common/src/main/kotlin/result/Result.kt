package common.src.main.kotlin.result

import austral.src.main.kotlin.commun.Position

sealed interface Result<out T, out E> {
    data class Success<out T>(val value: T) : Result<T, Nothing>
    data class Failure<out E>(val error: E) : Result<Nothing, E>
}

sealed interface CompilerError {
    val position: Position
    val message: String
}

data class SyntaxError(
    override val position: Position,
    override val message: String
) : CompilerError

data class SemanticError(
    override val position: Position,
    override val message: String
) : CompilerError

data class LexicalError(
    override val position: Position,
    override val message: String
) : CompilerError
