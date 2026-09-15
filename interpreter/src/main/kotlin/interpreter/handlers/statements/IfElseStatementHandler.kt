package interpreter.handlers.statements

import ast.Expr
import ast.IfStatement
import ast.Stmt
import interpreter.Output
import interpreter.StatementHandler
import runtime.Environment
import runtime.valuedataclass.BooleanValue
import runtime.valuedataclass.Value

class IfElseStatementHandler : StatementHandler {
    override fun canHandle(stmt: Stmt): Boolean = stmt is IfStatement

    override fun execute(
        stmt: Stmt,
        environment: Environment,
        evaluate: (Expr) -> Value,
        execute: (Stmt) -> Unit,
        output: Output,
    ) {
        val ifStatement = stmt as IfStatement

        val condition = evaluate(ifStatement.condition)

        require(condition is BooleanValue) {
            "If condition must be boolean"
        }

        if (condition.value) {
            environment.withScope {
                ifStatement.thenBranch.forEach(execute)
            }
        } else {
            ifStatement.elseBranch?.let { branch ->
                environment.withScope {
                    branch.forEach(execute)
                }
            }
        }
    }
}
