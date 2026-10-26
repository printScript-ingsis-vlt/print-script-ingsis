package linter.handlers.statements

import ast.Expr
import ast.Stmt
import ast.VariableDeclaration
import linter.StatementHandler

class VariableDeclarationHandler : StatementHandler {
    override fun canHandle(stmt: Stmt): Boolean = stmt is VariableDeclaration

    override fun expressions(stmt: Stmt): List<Expr> = listOfNotNull((stmt as VariableDeclaration).value)

    override fun identifier(stmt: Stmt): String = (stmt as VariableDeclaration).name
}
