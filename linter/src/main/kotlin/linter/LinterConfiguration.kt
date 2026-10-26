package linter

import ast.Expr
import ast.Stmt

data class LinterConfiguration(
    val statementHandlers: List<StatementHandler>,
    val expressionHandlers: List<ExpressionHandler>,
) {
    init {
        require(statementHandlers.isNotEmpty()) { "There is to be at least one statementHandler" }
        require(expressionHandlers.isNotEmpty()) { "There is to be at least one expressionHandler" }
    }

    fun statementHandler(stmt: Stmt): StatementHandler =
        statementHandlers.find { it.canHandle(stmt) }
            ?: error("No handler found for statement: ${stmt::class.simpleName}")

    fun expressionHandler(expr: Expr): ExpressionHandler =
        expressionHandlers.find { it.canHandle(expr) }
            ?: error("No expression handler found for: ${expr::class.simpleName}")
}
