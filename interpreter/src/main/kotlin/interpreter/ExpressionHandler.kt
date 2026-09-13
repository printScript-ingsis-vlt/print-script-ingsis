package interpreter

import ast.Expr
import runtime.Environment
import runtime.valuedataclass.Value

interface ExpressionHandler {
    fun canHandle(expr: Expr): Boolean

    fun evaluate(
        expr: Expr,
        environment: Environment,
        evaluate: (Expr) -> Value,
    ): Value
}
