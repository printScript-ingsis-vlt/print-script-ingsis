package formatter

import ast.Expr

interface ExpressionHandler {
    fun canHandle(expr: Expr): Boolean

    fun format(
        expr: Expr,
        formatExpression: (Expr) -> String,
    ): String
}
