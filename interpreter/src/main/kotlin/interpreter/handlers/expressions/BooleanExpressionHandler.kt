package interpreter.handlers.expressions

import ast.BooleanLiteral
import ast.Expr
import interpreter.ExecutionContext
import interpreter.ExpressionHandler
import runtime.valuedataclass.BooleanValue
import runtime.valuedataclass.Value

class BooleanExpressionHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean {
        return expr is BooleanLiteral
    }

    override fun evaluate(
        expr: Expr,
        context: ExecutionContext,
        evaluate: (Expr) -> Value,
    ): Value {
        val literal = expr as BooleanLiteral
        return BooleanValue(literal.value)
    }
}
