package semantic.handlers.expressions

import ast.Expr
import ast.StringLiteral
import semantic.SemanticContext
import semantic.expressions.ExpressionAnalysis
import semantic.expressions.ExpressionSemanticHandler

// Util principalmente en la 1.1 para validar readInput por ejemplo
class StringLiteralSemanticHandler : ExpressionSemanticHandler {
    override fun canHandle(expression: Expr): Boolean = expression is StringLiteral

    override fun analyze(
        expression: Expr,
        context: SemanticContext,
        analyzeChild: (Expr) -> ExpressionAnalysis,
    ): ExpressionAnalysis =
        ExpressionAnalysis(
            type = "string",
            errors = emptyList(),
            // knowNumberValue queda null, pero aceptable en este contexto, desestructurarlo mas seria demasiada complejidad
        )
}
