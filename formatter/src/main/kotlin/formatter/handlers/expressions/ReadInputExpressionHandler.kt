package formatter.handlers.expressions

import ast.Expr
import ast.ReadInputExpression
import formatter.ExpressionHandler

class ReadInputExpressionHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is ReadInputExpression

    override fun format(
        expr: Expr,
        formatExpression: (Expr) -> String,
    ): String = "readInput(${formatExpression((expr as ReadInputExpression).prompt)})"
}
