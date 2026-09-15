package semantic

import ast.Expr
import ast.Stmt
import result.SemanticError
import semantic.expressions.ExpressionAnalysis

/**
 * Valida y aplica los efectos semánticos de un tipo concreto de statement
 *
 * Cada implementación declara qué nodos sabe analizar mediante [canHandle]. El analyzer
 * selecciona exactamente un handler, ejecuta [validate] y llama a [updateEnvironment]
 * únicamente cuando no hay errores
 */
interface StatementSemanticHandler {
    // Si el handler sabe analizar un statement en particular
    fun canHandle(statement: Stmt): Boolean

    // Revisa reglas semanticas sin updatear nada
    fun validate(
        statement: Stmt,
        context: SemanticContext,
        analyzeExpression: (Expr) -> ExpressionAnalysis,
    ): List<SemanticError>

    // Aplica los efectos semanticos, idealmente solo luego de la validacion
    fun updateEnvironment(
        statement: Stmt,
        context: SemanticContext,
        analyzeExpression: (Expr) -> ExpressionAnalysis,
    )
}
