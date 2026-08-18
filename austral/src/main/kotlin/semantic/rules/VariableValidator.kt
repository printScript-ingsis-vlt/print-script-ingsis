package austral.src.main.kotlin.semantic.rules

import austral.src.main.kotlin.commun.result.SemanticError
import austral.src.main.kotlin.commun.Environment
import austral.src.main.kotlin.commun.ast.BinaryExpression
import austral.src.main.kotlin.commun.ast.Expr
import austral.src.main.kotlin.commun.ast.Identifier
import austral.src.main.kotlin.commun.ast.NumberLiteral
import austral.src.main.kotlin.commun.ast.Stmt
import austral.src.main.kotlin.commun.ast.StringLiteral
import austral.src.main.kotlin.commun.ast.VariableDeclaration
import austral.src.main.kotlin.semantic.SemanticRule

class VariableValidator : SemanticRule { // --> Valida que las variables existan y esten inicializadas

    override fun check(stmt: Stmt, environment: Environment): List<SemanticError> {
        val errors = mutableListOf<SemanticError>()
        when (stmt) {
            is VariableDeclaration -> {
                if (stmt.value != null) {
                    checkExpression(stmt.value, environment, errors)
                }
            }
        }
        return errors
    }

    private fun checkExpression(expr: Expr, env: Environment, errors: MutableList<SemanticError>) {
        when (expr) {
            is Identifier -> {
                val variable = env.lookup(expr.name)
                if (variable == null) {
                    errors.add(SemanticError(expr.position, "Variable '${expr.name}' is not declared"))
                } else if (variable.value == null) {
                    errors.add(SemanticError(expr.position, "Variable '${expr.name}' is not initialized"))
                }
            }
            is BinaryExpression -> {
                checkExpression(expr.left, env, errors)
                checkExpression(expr.right, env, errors)
            }
            is NumberLiteral -> {}
            is StringLiteral -> {}
        }
    }
}