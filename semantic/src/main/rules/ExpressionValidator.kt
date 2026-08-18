package austral.src.main.kotlin.semantic.rules

import austral.src.main.kotlin.commun.*
import austral.src.main.kotlin.commun.result.SemanticError
import austral.src.main.kotlin.interpreter.*
import austral.src.main.kotlin.semantic.SemanticRule

class ExpressionValidator : SemanticRule { // --> Valido operadores y que sea coherentes

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
            is BinaryExpression -> {
                if (expr.operator !in setOf("+", "-", "*", "/")) {
                    errors.add(SemanticError(expr.position, "Unknown operator '${expr.operator}'"))
                } else {
                    val leftType = resolveType(expr.left, env)
                    val rightType = resolveType(expr.right, env)

                    if (expr.operator != "+" && (leftType != "number" || rightType != "number")) {
                        errors.add(
                            SemanticError(
                                expr.position,
                                "Operator '${expr.operator}' requires operand"
                            )
                        )
                    }

                    if (expr.operator == "/" && errors.none { it.position == expr.position }) {
                        val rightValue = evaluateConstant(expr.right, env)
                        if (rightValue == 0.0) {
                            errors.add(SemanticError(expr.position, "Division by zero"))
                        }
                    }
                }

                checkExpression(expr.left, env, errors)
                checkExpression(expr.right, env, errors)
            }
            is Identifier -> {}
            is NumberLiteral -> {}
            is StringLiteral -> {}
        }
    }

    private fun resolveType(expr: Expr, env: Environment): String? = when (expr) {
        is NumberLiteral -> "number"
        is StringLiteral -> "string"
        is Identifier -> env.lookup(expr.name)?.type
        is BinaryExpression -> {
            val left = resolveType(expr.left, env) ?: return null
            val right = resolveType(expr.right, env) ?: return null
            if (expr.operator == "+" && (left == "string" || right == "string")) "string"
            else if (left == "number" && right == "number") "number"
            else null
        }
    }

    private fun evaluateConstant(expr: Expr, env: Environment): Double? = when (expr) {
        is NumberLiteral -> expr.value
        is Identifier -> (env.lookup(expr.name)?.value as? NumberValue)?.value
        is BinaryExpression -> {
            val left = evaluateConstant(expr.left, env) ?: return null
            val right = evaluateConstant(expr.right, env) ?: return null
            when (expr.operator) {
                "+" -> left + right
                "-" -> left - right
                "*" -> left * right
                "/" -> if (right != 0.0) left / right else null
                else -> null
            }
        }
        is StringLiteral -> null
    }
}