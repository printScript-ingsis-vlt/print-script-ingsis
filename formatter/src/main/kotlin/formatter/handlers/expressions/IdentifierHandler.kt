package formatter.handlers.expressions

import ast.Expr
import ast.Identifier
import formatter.ExpressionHandler

class IdentifierHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is Identifier

    override fun format(
        expr: Expr,
        formatExpression: (Expr) -> String,
    ): String = (expr as Identifier).name
}
