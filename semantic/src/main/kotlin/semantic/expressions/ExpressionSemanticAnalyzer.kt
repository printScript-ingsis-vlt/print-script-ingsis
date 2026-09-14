package semantic.expressions

import ast.Expr
import semantic.SemanticContext

/** Selecciona el handler semántico y coordina el análisis recursivo de expresiones. */
class ExpressionSemanticAnalyzer(
    private val handlers: List<ExpressionSemanticHandler>,
) {
    init {
        require(handlers.isNotEmpty()) {
            "ExpressionSemanticAnalyzer requires at least one handler"
        }
    }

    fun analyze(
        expression: Expr,
        context: SemanticContext,
    ): ExpressionAnalysis {
        val candidates = handlers.filter { it.canHandle(expression) }

        val handler =
            when (candidates.size) {
                0 -> error("No semantic expression handler found for: ${expression::class.simpleName}")
                1 -> candidates.single()
                else ->
                    error(
                        "Ambiguous semantic expression handlers for " +
                            "${expression::class.simpleName}: " +
                            candidates.joinToString { it::class.simpleName.orEmpty() },
                    )
            }
        // ej: BinaryExpression -> analyzeChild -> Identifier, NumberLiteral
        return handler.analyze(expression, context) { child -> analyze(child, context) }
    }
}
