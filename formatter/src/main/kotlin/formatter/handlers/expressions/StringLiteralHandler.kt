package formatter.handlers.expressions

import ast.Expr
import ast.StringLiteral
import formatter.ExpressionHandler

class StringLiteralHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is StringLiteral

    override fun format(
        expr: Expr,
        formatExpression: (Expr) -> String,
    ): String = "\"${(expr as StringLiteral).value}\""
}
