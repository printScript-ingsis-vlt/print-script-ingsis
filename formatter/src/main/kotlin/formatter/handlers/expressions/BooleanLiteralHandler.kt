package formatter.handlers.expressions

import ast.BooleanLiteral
import ast.Expr
import formatter.ExpressionHandler

class BooleanLiteralHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is BooleanLiteral

    override fun format(
        expr: Expr,
        formatExpression: (Expr) -> String,
    ): String = (expr as BooleanLiteral).value.toString()
}
