package semantic.expressions

import ast.Expr
import semantic.SemanticContext

/**
 * Analiza semánticamente un tipo concreto de expresión
 */
interface ExpressionSemanticHandler {
    fun canHandle(expression: Expr): Boolean

    // analyzeChild permite que los handlers compuestos deleguen el análisis de sus expresiones
    // hijas al dispatcher, sin depender de implementaciones concretas de otros handlers
    fun analyze(
        expression: Expr,
        context: SemanticContext,
        analyzeChild: (Expr) -> ExpressionAnalysis,
    ): ExpressionAnalysis
}
