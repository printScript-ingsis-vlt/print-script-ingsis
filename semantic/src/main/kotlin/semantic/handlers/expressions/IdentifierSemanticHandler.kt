package semantic.handlers.expressions

import ast.Expr
import ast.Identifier
import result.SemanticError
import runtime.valuedataclass.NumberValue
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
        val variable = context.environment.lookup(identifier.name)

        // Variable no declarada
        if (variable == null) {
            return ExpressionAnalysis(
                type = null,
                errors = listOf(SemanticError(identifier.position, "Variable '${identifier.name}' is not declared")),
            )
        }

        // Variable declarada sin inicializar
        val errors =
            if (variable.value == null) {
                listOf(SemanticError(identifier.position, "Variable '${identifier.name}' is not initialized"))
            } else {
                emptyList()
            }

        return ExpressionAnalysis(
            type = variable.type,
            errors = errors,
            knownNumberValue = (variable.value as? NumberValue)?.value,
        )
    }
}
