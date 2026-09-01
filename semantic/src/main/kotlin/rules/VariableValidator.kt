package rules

import SemanticRule
import recurses.*
import result.SemanticError

class VariableValidator : SemanticRule { // --> Valida que las variables existan y esten inicializadas

    override fun check(stmt: Stmt, environment: Environment): List<SemanticError> {
        val errors = mutableListOf<SemanticError>()
        when (stmt) {
            is VariableDeclaration -> {
                val value = stmt.value
                if (value != null) {
                    checkExpression(value, environment, errors)
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
