package linter.handlers.statements

import ast.Expr
import ast.IfStatement
import ast.Stmt
import linter.StatementHandler

class IfStatementHandler : StatementHandler {
    override fun canHandle(stmt: Stmt): Boolean = stmt is IfStatement

    override fun expressions(stmt: Stmt): List<Expr> = listOf((stmt as IfStatement).condition)

    override fun statements(stmt: Stmt): List<Stmt> {
        val statement = stmt as IfStatement
        return statement.thenBranch + statement.elseBranch.orEmpty()
    }
}
