package formatter.handlers.expressions

import ast.Expr
import ast.NumberLiteral
import formatter.ExpressionHandler

class NumberLiteralHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is NumberLiteral

    override fun format(
        expr: Expr,
        formatExpression: (Expr) -> String,
    ): String {
        val value = (expr as NumberLiteral).value
        return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
    }
}
