package linter.handlers.statements

import ast.Expr
import ast.PrintStatement
import ast.Stmt
import linter.StatementHandler

class PrintStatementHandler : StatementHandler {
    override fun canHandle(stmt: Stmt): Boolean = stmt is PrintStatement

    override fun expressions(stmt: Stmt): List<Expr> = listOf((stmt as PrintStatement).argument)
}
