package linter.handlers.expressions

import ast.Expr
import ast.ReadInputExpression
import linter.ExpressionHandler

class ReadInputExpressionHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is ReadInputExpression

    override fun children(expr: Expr): List<Expr> = listOf((expr as ReadInputExpression).prompt)
}
