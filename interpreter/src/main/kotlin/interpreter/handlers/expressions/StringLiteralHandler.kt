package interpreter.handlers.expressions

import ast.Expr
import ast.StringLiteral
import interpreter.ExecutionContext
import interpreter.ExpressionHandler
import runtime.valuedataclass.StringValue
import runtime.valuedataclass.Value

class StringLiteralHandler : ExpressionHandler {
    override fun canHandle(expr: Expr) = expr is StringLiteral

    override fun evaluate(
        expr: Expr,
        context: ExecutionContext,
        evaluate: (Expr) -> Value,
    ): Value {
        val literal = expr as StringLiteral
        return StringValue(literal.value)
    }
}
