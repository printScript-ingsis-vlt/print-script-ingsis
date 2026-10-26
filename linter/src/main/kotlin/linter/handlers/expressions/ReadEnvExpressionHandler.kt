package linter.handlers.expressions

import ast.Expr
import ast.ReadEnvExpression
import linter.ExpressionHandler

class ReadEnvExpressionHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is ReadEnvExpression

    override fun children(expr: Expr): List<Expr> = listOf((expr as ReadEnvExpression).envVariableName)
}
