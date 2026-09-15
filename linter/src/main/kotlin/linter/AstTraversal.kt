package linter

import ast.Assignment
import ast.BinaryExpression
import ast.Expr
import ast.IfStatement
import ast.PrintStatement
import ast.Program
import ast.ReadEnvExpression
import ast.ReadInputExpression
import ast.Stmt
import ast.VariableDeclaration

/** Recorre todas las expresiones de un programa, incluso dentro de bloques y expresiones compuestas. */
object AstTraversal {
    // Visita primero una expresion compuesta y luego sus hijos
    fun forEachExpression(
        program: Program,
        visit: (Expr) -> Unit,
    ) {
        program.statements.forEach { statement -> visitStatement(statement, visit) }
    }

    private fun visitStatement(
        statement: Stmt,
        visit: (Expr) -> Unit,
    ) {
        when (statement) {
            is VariableDeclaration -> statement.value?.let { expression -> visitExpression(expression, visit) }
            is Assignment -> visitExpression(statement.value, visit)
            is PrintStatement -> visitExpression(statement.argument, visit)
            is IfStatement -> {
                visitExpression(statement.condition, visit)
                statement.thenBranch.forEach { branchStatement -> visitStatement(branchStatement, visit) }
                statement.elseBranch?.forEach { branchStatement -> visitStatement(branchStatement, visit) }
            }
        }
    }

    private fun visitExpression(
        expression: Expr,
        visit: (Expr) -> Unit,
    ) {
        visit(expression)

        when (expression) {
            is BinaryExpression -> {
                visitExpression(expression.left, visit)
                visitExpression(expression.right, visit)
            }
            is ReadInputExpression -> visitExpression(expression.prompt, visit)
            is ReadEnvExpression -> visitExpression(expression.envVariableName, visit)
            else -> Unit
        }
    }
}
