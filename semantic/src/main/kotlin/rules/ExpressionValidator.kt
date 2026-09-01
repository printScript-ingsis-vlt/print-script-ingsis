package rules

import SemanticRule
import recurses.*
import recurses.valuedataclass.NumberValue
import result.SemanticError

class ExpressionValidator : SemanticRule { // --> Valido operadores y que sea coherentes

    override fun check(stmt: Stmt, environment: Environment): List<SemanticError> {
        return stmt.getExpressions().flatMap { checkExpression(it, environment) }
    }

    fun Stmt.getExpressions(): List<Expr> = when (this) {
        is VariableDeclaration -> listOfNotNull(this.value)
        is Assignment -> listOf(this.value)
        is PrintStatement -> listOf(this.argument)
    }

    private fun checkExpression(expr: Expr, env: Environment): List<SemanticError>  {
        var errors = mutableListOf<SemanticError>()
        when (expr) {
            is BinaryExpression -> {
                // --> Operador no valido
                if (expr.operator !in setOf("+", "-", "*", "/")) {
                    errors.add(SemanticError(expr.position, "Unknown operator '${expr.operator}'"))
                } else {
                    val leftType = resolveType(expr.left, env)
                    val rightType = resolveType(expr.right, env)

                    noStringsOperation(expr, leftType, rightType, errors)
                    noDivisionByZero(expr, errors, env)
                }

                // --> Recursion a los hijos
                errors.addAll(checkExpression(expr.left, env))
                errors.addAll(checkExpression(expr.right, env))
            }
            is Identifier, is NumberLiteral, is StringLiteral -> {}
        }
        return errors;
    }

    private fun noDivisionByZero(
        expr: BinaryExpression,
        errors: MutableList<SemanticError>,
        env: Environment
    ) {
        if (expr.operator == "/" && errors.none { it.position == expr.position }) {
            val rightValue = evaluateConstant(expr.right, env)
            if (rightValue == 0.0) {
                errors.add(SemanticError(expr.position, "Division by zero"))
            }
        }
    }

    private fun noStringsOperation(
        expr: BinaryExpression,
        leftType: String?,
        rightType: String?,
        errors: MutableList<SemanticError>
    ) {
        if (expr.operator != "+" && (leftType != "number" || rightType != "number")) {
            errors.add(
                SemanticError(
                    expr.position,
                    "Operator '${expr.operator}' requires operand"
                )
            )
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
