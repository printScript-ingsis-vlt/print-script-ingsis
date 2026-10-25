package linter

import ast.Expr
import ast.Stmt

interface StatementHandler {
    fun canHandle(stmt: Stmt): Boolean

    fun expressions(stmt: Stmt): List<Expr> = emptyList()

    fun statements(stmt: Stmt): List<Stmt> = emptyList()

    fun identifier(stmt: Stmt): String? = null
}
