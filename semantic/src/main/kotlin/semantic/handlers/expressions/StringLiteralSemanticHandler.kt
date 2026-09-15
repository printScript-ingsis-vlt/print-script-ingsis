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
        expectedType: String?,
        analyzeChild: (Expr, String?) -> ExpressionAnalysis,
    ): ExpressionAnalysis =
        ExpressionAnalysis(
            type = "string",
            errors = emptyList(),
            // knownNumberValue queda null: un string no tiene valor numérico estático.
        )
}
