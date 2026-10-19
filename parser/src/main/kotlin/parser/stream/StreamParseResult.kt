package parser.stream

import result.SyntaxError

sealed interface StreamParseResult {
    data class Success(val value: Any?) : StreamParseResult

    data class Failure(val error: SyntaxError) : StreamParseResult
}
