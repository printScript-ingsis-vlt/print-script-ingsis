package linter

import ast.Expr

interface ExpressionHandler {
    fun canHandle(expr: Expr): Boolean

    fun children(expr: Expr): List<Expr> = emptyList()
}
