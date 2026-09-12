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
import semantic.SemanticRule

class VariableValidator : SemanticRule { // --> Valida que las variables existan y esten inicializadas

    override fun check(
        stmt: Stmt,
        environment: Environment,
    ): List<SemanticError> {
        val errors = mutableListOf<SemanticError>()
        when (stmt) {
            is VariableDeclaration -> {
                val value = stmt.value
                if (value != null) {
                    checkExpression(value, environment, errors)
                }
            }
            is PrintStatement -> {
                checkExpression(stmt.argument, environment, errors)
            }
            is Assignment -> {
                assiCase(environment, stmt, errors)
            }
        }
        return errors
    }

    private fun assiCase(
        environment: Environment,
        stmt: Assignment,
        errors: MutableList<SemanticError>,
    ) {
        val variable = environment.lookup(stmt.name)
        if (variable == null) {
            errors.add(SemanticError(stmt.position, "Variable '${stmt.name}' is not declared"))
        }
        checkExpression(stmt.value, environment, errors)
    }

    private fun checkExpression(
        expr: Expr,
        env: Environment,
        errors: MutableList<SemanticError>,
    ) {
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
