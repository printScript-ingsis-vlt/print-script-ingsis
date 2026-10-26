package linter

import ast.Expr
import ast.Program
import ast.Stmt

/** Recorre todas las expresiones de un programa, incluso dentro de bloques y expresiones compuestas. */
object AstTraversal {
    // Visita primero una expresion compuesta y luego sus hijos
    fun forEachExpression(
        program: Program,
        visit: (Expr) -> Unit,
    ) = forEachExpression(program, LinterConfigurations.default, visit)

    fun forEachExpression(
        program: Program,
        configuration: LinterConfiguration,
        visit: (Expr) -> Unit,
    ) {
        program.statements.forEach { statement -> visitStatement(statement, configuration, visit) }
    }

    private fun visitStatement(
        statement: Stmt,
        configuration: LinterConfiguration,
        visit: (Expr) -> Unit,
    ) {
        val handler = configuration.statementHandler(statement)
        handler.expressions(statement).forEach { expression -> visitExpression(expression, configuration, visit) }
        handler.statements(statement).forEach { child -> visitStatement(child, configuration, visit) }
    }

    private fun visitExpression(
        expression: Expr,
        configuration: LinterConfiguration,
        visit: (Expr) -> Unit,
    ) {
        visit(expression)
        configuration.expressionHandler(expression).children(expression).forEach { child ->
            visitExpression(child, configuration, visit)
        }
    }
}
