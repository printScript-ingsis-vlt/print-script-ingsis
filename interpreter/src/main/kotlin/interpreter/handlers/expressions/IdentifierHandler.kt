package interpreter.handlers.expressions

import ast.Expr
import ast.Identifier
import interpreter.ExpressionHandler
import interpreter.InputProvider
import interpreter.Output
import runtime.Environment
import runtime.valuedataclass.Value

class IdentifierHandler : ExpressionHandler {
    override fun canHandle(expr: Expr) = expr is Identifier

    override fun evaluate(
        expr: Expr,
        environment: Environment,
        evaluate: (Expr) -> Value,
        inputProvider: InputProvider,
        output: Output,
    ): Value {
        val id = expr as Identifier
        val variable =
            environment.lookup(id.name)
                ?: error("Undefined variable '${id.name}'")
        return variable.value
            ?: error("Variable '${id.name}' is not initialized")
    }
}
