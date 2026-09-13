package handlers.statements

import ast.Assignment
import ast.Expr
import ast.Stmt
import interpreter.Output
import interpreter.StatementHandler
import runtime.Environment
import runtime.valuedataclass.Value

class AssignmentHandler : StatementHandler {
    override fun canHandle(stmt: Stmt) = stmt is Assignment

    override fun execute(
        stmt: Stmt,
        environment: Environment,
        evaluate: (Expr) -> Value,
        output: Output,
    ) {
        val assign = stmt as Assignment
        val value = evaluate(assign.value)
        environment.assign(assign.name, value)
    }
}
