package interpreter

import ast.Expr
import ast.Program
import ast.Stmt
import runtime.Environment
import runtime.valuedataclass.Value

class ConfigurableInterpreter(
    private val output: Output,
    private val inputProvider: InputProvider = StdinInputProvider(),
    private val envProvider: EnvProvider = SystemEnvProvider(),
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

        val context = ExecutionContext(environment, output, inputProvider, envProvider)
        return handler.evaluate(expr, context, ::evaluate)
    }
}
