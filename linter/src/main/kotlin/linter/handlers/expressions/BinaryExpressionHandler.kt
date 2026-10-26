package linter.handlers.expressions

import ast.BinaryExpression
import ast.Expr
import linter.ExpressionHandler

class BinaryExpressionHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is BinaryExpression

    override fun children(expr: Expr): List<Expr> {
        val binary = expr as BinaryExpression
        return listOf(binary.left, binary.right)
    }
}
