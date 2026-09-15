package interpreter.handlers.expressions

import ast.Expr
import ast.Identifier
import interpreter.ExecutionContext
import interpreter.ExpressionHandler
import runtime.valuedataclass.Value

class IdentifierHandler : ExpressionHandler {
    override fun canHandle(expr: Expr) = expr is Identifier

    override fun evaluate(
        expr: Expr,
        context: ExecutionContext,
        evaluate: (Expr) -> Value,
    ): Value {
        val id = expr as Identifier
        val variable =
            context.environment.lookup(id.name)
                ?: error("Undefined variable '${id.name}'")
        return variable.value
            ?: error("Variable '${id.name}' is not initialized")
    }
}
