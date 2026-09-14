package interpreter

import ast.Expr
import ast.Program
import ast.Stmt
import interpreter.handlers.expressions.BinaryExpressionHandler
import interpreter.handlers.expressions.IdentifierHandler
import interpreter.handlers.expressions.NumberLiteralHandler
import interpreter.handlers.expressions.StringLiteralHandler
import interpreter.handlers.statements.AssignmentHandler
import interpreter.handlers.statements.PrintStatementHandler
import interpreter.handlers.statements.VariableDeclarationHandler
import runtime.Environment
import runtime.valuedataclass.Value

class ConfigurableInterpreter(private val output: Output) {
    private val environment = Environment()

    // Lista de handlers (fácil de extender)
    private val statementHandlers: List<StatementHandler> =
        listOf(
            VariableDeclarationHandler(),
            AssignmentHandler(),
            PrintStatementHandler(),
        )
    private val expressionHandlers: List<ExpressionHandler> =
        listOf(
            NumberLiteralHandler(),
            StringLiteralHandler(),
            IdentifierHandler(),
            BinaryExpressionHandler(),
        )

    fun run(program: Program) {
        program.statements.forEach(::execute)
    }

    private fun execute(stmt: Stmt) {
        val handler =
            statementHandlers.find { it.canHandle(stmt) }
                ?: error("No handler found for statement: ${stmt::class.simpleName}")

        handler.execute(stmt, environment, ::evaluate, output)
    }

    fun evaluate(expr: Expr): Value {
        val handler =
            expressionHandlers.find { it.canHandle(expr) }
                ?: error("No expression handler found for: ${expr::class.simpleName}")

        return handler.evaluate(expr, environment, ::evaluate)
    }
}
