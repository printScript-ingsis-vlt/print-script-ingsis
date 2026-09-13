package interpreter.handlers.statements

import ast.Expr
import ast.PrintStatement
import ast.Stmt
import interpreter.Output
import interpreter.StatementHandler
import runtime.Environment
import runtime.valuedataclass.Value

class PrintStatementHandler : StatementHandler {
    override fun canHandle(stmt: Stmt) = stmt is PrintStatement

    override fun execute(
        stmt: Stmt,
        environment: Environment,
        evaluate: (Expr) -> Value,
        output: Output,
    ) {
        val print = stmt as PrintStatement
        val result = evaluate(print.argument)
        output.write(result.asString())
    }
}
