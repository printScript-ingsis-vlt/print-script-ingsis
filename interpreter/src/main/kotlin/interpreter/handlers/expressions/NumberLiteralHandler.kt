package interpreter.handlers.expressions

import ast.Expr
import ast.NumberLiteral
import interpreter.ExecutionContext
import interpreter.ExpressionHandler
import runtime.valuedataclass.NumberValue
import runtime.valuedataclass.Value

class NumberLiteralHandler : ExpressionHandler {
    override fun canHandle(expr: Expr) = expr is NumberLiteral

    override fun evaluate(
        expr: Expr,
        context: ExecutionContext,
        evaluate: (Expr) -> Value,
    ): Value {
        val literal = expr as NumberLiteral
        return NumberValue(literal.value)
    }
}
