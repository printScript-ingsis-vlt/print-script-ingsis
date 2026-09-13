package interpreter

import ast.Expr
import ast.Stmt
import runtime.Environment
import runtime.valuedataclass.Value

interface StatementHandler {
    fun canHandle(stmt: Stmt): Boolean
    fun execute(stmt: Stmt, environment: Environment, evaluate: (Expr) -> Value, output: Output)
}
