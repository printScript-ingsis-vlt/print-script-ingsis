package linter.handlers.statements

import ast.Assignment
import ast.Expr
import ast.Stmt
import linter.StatementHandler

class AssignmentHandler : StatementHandler {
    override fun canHandle(stmt: Stmt): Boolean = stmt is Assignment

    override fun expressions(stmt: Stmt): List<Expr> = listOf((stmt as Assignment).value)

    override fun identifier(stmt: Stmt): String = (stmt as Assignment).name
}
