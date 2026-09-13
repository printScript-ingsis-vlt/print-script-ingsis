package handlers.expressions

import ast.Expr
import ast.NumberLiteral
import interpreter.ExpressionHandler
import runtime.Environment
import runtime.valuedataclass.NumberValue
import runtime.valuedataclass.Value

class NumberLiteralHandler : ExpressionHandler {
    override fun canHandle(expr: Expr) = expr is NumberLiteral

    override fun evaluate(
        expr: Expr,
        environment: Environment,
        evaluate: (Expr) -> Value,
    ): Value {
        val literal = expr as NumberLiteral
        return NumberValue(literal.value)
    }
}
