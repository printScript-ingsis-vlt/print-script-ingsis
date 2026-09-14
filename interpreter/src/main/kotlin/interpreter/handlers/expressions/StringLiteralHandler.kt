package interpreter.handlers.expressions

import ast.Expr
import ast.StringLiteral
import interpreter.ExpressionHandler
import runtime.Environment
import runtime.valuedataclass.StringValue
import runtime.valuedataclass.Value

class StringLiteralHandler : ExpressionHandler {
    override fun canHandle(expr: Expr) = expr is StringLiteral

    override fun evaluate(
        expr: Expr,
        environment: Environment,
        evaluate: (Expr) -> Value,
    ): Value {
        val literal = expr as StringLiteral
        return StringValue(literal.value)
    }
}
