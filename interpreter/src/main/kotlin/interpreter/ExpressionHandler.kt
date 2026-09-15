package interpreter

import ast.Expr
import runtime.valuedataclass.Value

interface ExpressionHandler {
    fun canHandle(expr: Expr): Boolean

    fun evaluate(
        expr: Expr,
        context: ExecutionContext,
        evaluate: (Expr) -> Value,
    ): Value
}
