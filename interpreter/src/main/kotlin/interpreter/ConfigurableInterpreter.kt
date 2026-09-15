package interpreter

import ast.Expr
import ast.Program
import ast.Stmt
import runtime.Environment
import runtime.valuedataclass.Value

class ConfigurableInterpreter(
    private val output: Output,
    configuration: InterpreterConfiguration = InterpreterConfigurations.default,
) {
    private val environment = Environment()

    private val statementHandlers = configuration.statementHandlers
    private val expressionHandlers = configuration.expressionHandlers

    fun run(program: Program) {
        program.statements.forEach(::execute)
    }

    private fun execute(stmt: Stmt) {
        val handler =
            statementHandlers.find { it.canHandle(stmt) }
                ?: error("No handler found for statement: ${stmt::class.simpleName}")

        handler.execute(stmt, environment, ::evaluate, ::execute, output)
    }

    fun evaluate(expr: Expr): Value {
        val handler =
            expressionHandlers.find { it.canHandle(expr) }
                ?: error("No expression handler found for: ${expr::class.simpleName}")

        return handler.evaluate(expr, environment, ::evaluate)
    }
}
