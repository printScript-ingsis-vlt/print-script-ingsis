package semantic.handlers.expressions

import ast.Expr
import ast.Identifier
import result.SemanticError
import semantic.SemanticContext
import semantic.expressions.ExpressionAnalysis
import semantic.expressions.ExpressionSemanticHandler

class IdentifierSemanticHandler : ExpressionSemanticHandler {
    override fun canHandle(expression: Expr): Boolean = expression is Identifier

    override fun analyze(
        expression: Expr,
        context: SemanticContext,
        analyzeChild: (Expr) -> ExpressionAnalysis,
    ): ExpressionAnalysis {
        val identifier = expression as Identifier
        val symbol = context.symbols.lookup(identifier.name)

        // Variable no declarada
        if (symbol == null) {
            return ExpressionAnalysis(
                type = null,
                errors = listOf(SemanticError(identifier.position, "Variable '${identifier.name}' is not declared")),
            )
        }

        // Variable declarada sin inicializar
        val errors =
            if (!symbol.initialized) {
                listOf(SemanticError(identifier.position, "Variable '${identifier.name}' is not initialized"))
            } else {
                emptyList()
            }

        return ExpressionAnalysis(
            type = symbol.type,
            errors = errors,
            knownNumberValue = symbol.knownNumberValue,
        )
    }
}
