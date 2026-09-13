package interpreter

import ast.Expr
import ast.Program
import ast.Stmt
import handlers.expressions.BinaryExpressionHandler
import handlers.expressions.IdentifierHandler
import handlers.expressions.NumberLiteralHandler
import handlers.expressions.StringLiteralHandler
import handlers.statements.AssignmentHandler
import handlers.statements.PrintStatementHandler
import handlers.statements.VariableDeclarationHandler
import runtime.Environment
import runtime.valuedataclass.Value

class ConfigurableInterpreter(private val output: Output) {
    private val environment = Environment()

    // Lista de handlers (fácil de extender)
    private val statementHandlers: List<StatementHandler> = listOf(
        VariableDeclarationHandler(),
        AssignmentHandler(),
        PrintStatementHandler(),
    )
    private val expressionHandlers: List<ExpressionHandler> = listOf(
        NumberLiteralHandler(),
        StringLiteralHandler(),
        IdentifierHandler(),
        BinaryExpressionHandler(),
    )

    fun run(program: Program) {
        program.statements.forEach(::execute)
    }

    private fun execute(stmt: Stmt) {
        val handler = statementHandlers.find { it.canHandle(stmt) }
            ?: error("No handler found for statement: ${stmt::class.simpleName}")

        handler.execute(stmt, environment, ::evaluate, output)
    }

    fun evaluate(expr: Expr): Value {
        val handler = expressionHandlers.find { it.canHandle(expr) }
            ?: error("No expression handler found for: ${expr::class.simpleName}")

        return handler.evaluate(expr, environment, ::evaluate)
    }

}
