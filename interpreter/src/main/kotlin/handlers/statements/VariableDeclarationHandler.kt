package handlers.statements

import ast.Expr
import ast.Stmt
import ast.VariableDeclaration
import interpreter.Output
import interpreter.StatementHandler
import runtime.Environment
import runtime.Variable
import runtime.valuedataclass.Value

class VariableDeclarationHandler : StatementHandler {
    override fun canHandle(stmt: Stmt) = stmt is VariableDeclaration

    override fun execute(
        stmt: Stmt,
        environment: Environment,
        evaluate: (Expr) -> Value,
        output: Output,
    ) {
        val decl = stmt as VariableDeclaration
        val value = decl.value?.let(evaluate)
        environment.declare(decl.name, Variable(decl.type, value))
    }
}
