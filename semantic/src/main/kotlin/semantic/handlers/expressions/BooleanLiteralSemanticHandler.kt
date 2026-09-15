package semantic.handlers.expressions

import ast.BooleanLiteral
import ast.Expr
import semantic.SemanticContext
import semantic.expressions.ExpressionAnalysis
import semantic.expressions.ExpressionSemanticHandler

// Infiere el tipo boolean para los literales true y false de la 1.1
class BooleanLiteralSemanticHandler : ExpressionSemanticHandler {
    override fun canHandle(expression: Expr): Boolean = expression is BooleanLiteral

    override fun analyze(
        expression: Expr,
        context: SemanticContext,
        analyzeChild: (Expr) -> ExpressionAnalysis,
    ): ExpressionAnalysis =
        ExpressionAnalysis(
            type = "boolean",
            errors = emptyList(),
        )
}
