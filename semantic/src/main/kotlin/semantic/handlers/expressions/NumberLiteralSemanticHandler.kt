package semantic.handlers.expressions

import ast.Expr
import ast.NumberLiteral
import semantic.SemanticContext
import semantic.expressions.ExpressionAnalysis
import semantic.expressions.ExpressionSemanticHandler

// Muy puntual. Permite al saber un valor numerico detectar expresiones como dividir por cero
class NumberLiteralSemanticHandler : ExpressionSemanticHandler {
    override fun canHandle(expression: Expr): Boolean = expression is NumberLiteral

    override fun analyze(
        expression: Expr,
        context: SemanticContext,
        analyzeChild: (Expr) -> ExpressionAnalysis,
    ): ExpressionAnalysis {
        val literal = expression as NumberLiteral

        return ExpressionAnalysis(
            type = "number",
            errors = emptyList(),
            knownNumberValue = literal.value,
        )
    }
}
