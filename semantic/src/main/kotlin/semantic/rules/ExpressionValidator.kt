package semantic.rules

import ast.Assignment
import ast.BinaryExpression
import ast.Expr
import ast.Identifier
import ast.NumberLiteral
import ast.PrintStatement
import ast.Stmt
import ast.StringLiteral
import ast.VariableDeclaration
import result.SemanticError
import runtime.Environment
import runtime.OperationType
import runtime.valuedataclass.NumberValue
import semantic.SemanticRule

class ExpressionValidator : SemanticRule { // --> Valido operadores y que sea coherentes

    override fun check(
        stmt: Stmt,
        environment: Environment,
    ): List<SemanticError> {
        return stmt.getExpressions().flatMap { checkExpression(it, environment) }
    }

    private fun operationTypeOrNull(operator: String): OperationType? =
        runCatching { OperationType.fromString(operator) }.getOrNull()

    fun Stmt.getExpressions(): List<Expr> =
        when (this) {
            is VariableDeclaration -> listOfNotNull(this.value)
            is Assignment -> listOf(this.value)
            is PrintStatement -> listOf(this.argument)
        }

    private fun checkExpression(
        expr: Expr,
        env: Environment,
    ): List<SemanticError> {
        var errors = mutableListOf<SemanticError>()
        when (expr) {
            is BinaryExpression -> {
                // --> Operador no valido
                val operationType = operationTypeOrNull(expr.operator)
                if (operationType == null) {
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
        return errors
    }

    private fun noDivisionByZero(
        expr: BinaryExpression,
        errors: MutableList<SemanticError>,
        env: Environment,
    ) {
        val isDivision = operationTypeOrNull(expr.operator) == OperationType.DIVIDE
        if (isDivision && errors.none { it.position == expr.position }) {
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
        errors: MutableList<SemanticError>,
    ) {
        val isPlus = operationTypeOrNull(expr.operator) == OperationType.PLUS
        if (!isPlus && (leftType != "number" || rightType != "number")) {
            errors.add(
                SemanticError(
                    expr.position,
                    "Operator '${expr.operator}' requires operand",
                ),
            )
        }
    }

    private fun resolveType(
        expr: Expr,
        env: Environment,
    ): String? =
        when (expr) {
            is NumberLiteral -> "number"
            is StringLiteral -> "string"
            is Identifier -> env.lookup(expr.name)?.type
            is BinaryExpression -> {
                val left = resolveType(expr.left, env) ?: return null
                val right = resolveType(expr.right, env) ?: return null
                val isPlus = operationTypeOrNull(expr.operator) == OperationType.PLUS
                if (isPlus && (left == "string" || right == "string")) {
                    "string"
                } else if (left == "number" && right == "number") {
                    "number"
                } else {
                    null
                }
            }
        }

    private fun evaluateConstant(
        expr: Expr,
        env: Environment,
    ): Double? =
        when (expr) {
            is NumberLiteral -> expr.value
            is Identifier -> (env.lookup(expr.name)?.value as? NumberValue)?.value
            is BinaryExpression -> {
                val left = evaluateConstant(expr.left, env) ?: return null
                val right = evaluateConstant(expr.right, env) ?: return null
                val operationType = operationTypeOrNull(expr.operator) ?: return null
                when (operationType) {
                    OperationType.PLUS -> left + right
                    OperationType.MINUS -> left - right
                    OperationType.MULTIPLY -> left * right
                    OperationType.DIVIDE -> if (right != 0.0) left / right else null
                }
            }
            is StringLiteral -> null
        }
}
