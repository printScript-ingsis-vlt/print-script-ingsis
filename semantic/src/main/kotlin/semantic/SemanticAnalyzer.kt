package semantic

import ast.Program
import ast.Stmt
import result.SemanticError

/** Orquesta los handlers semánticos y conserva el contexto de un programa completo. */
class SemanticAnalyzer(
    private val statementHandlers: List<StatementSemanticHandler> = DefaultSemanticConfiguration.statementHandlers(),
) {
    init {
        require(statementHandlers.isNotEmpty()) {
            "SemanticAnalyzer requires at least one statement handler"
        }
    }

    fun analyze(program: Program): List<SemanticError> {
        val context = SemanticContext()
        val errors = mutableListOf<SemanticError>()
        for (statement in program.statements) {
            val handler = handlerFor(statement)
            val statementErrors = handler.validate(statement, context)

            errors.addAll(statementErrors)
            if (statementErrors.isEmpty()) {
                handler.updateEnvironment(statement, context)
            }
        }

        return errors
    }

    private fun handlerFor(statement: Stmt): StatementSemanticHandler {
        val candidates = statementHandlers.filter { it.canHandle(statement) }

        return when (candidates.size) {
            0 -> error("No semantic statement handler found for: ${statement::class.simpleName}")
            1 -> candidates.single()
            else ->
                error(
                    "Ambiguous semantic statement handlers for " +
                        "${statement::class.simpleName}: " +
                        candidates.joinToString { it::class.simpleName.orEmpty() },
                )
        }
    }
}
