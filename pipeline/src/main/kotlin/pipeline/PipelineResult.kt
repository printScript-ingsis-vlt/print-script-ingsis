package pipeline

import result.LexicalError
import result.SemanticError
import result.SyntaxError

sealed interface PipelineResult {
    object Success : PipelineResult

    data class LexicalFailure(val error: LexicalError) : PipelineResult

    data class SyntaxFailure(val error: SyntaxError) : PipelineResult

    data class SemanticFailure(val errors: List<SemanticError>) : PipelineResult
}
