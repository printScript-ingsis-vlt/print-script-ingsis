package rules

import SemanticRule
import recurses.*
import result.SemanticError

class DeclarationValidator : SemanticRule { // --> Valida los tipos y que el valor declarado coincida con el tipo

    override fun check(stmt: Stmt, environment: Environment): List<SemanticError> {
        val errors = mutableListOf<SemanticError>()
        when (stmt) {
            is VariableDeclaration -> {
                vdCase(stmt, errors, environment)
            }
            is PrintStatement -> {
                // --> Print() acepta cualquier tipo valido
            }
            is Assignment -> {
                assiCase(stmt, errors, environment)
            }
        }
        return errors
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

    private fun vdCase(
        stmt: VariableDeclaration,
        errors: MutableList<SemanticError>,
        environment: Environment
    ) {
        if (stmt.type != "number" && stmt.type != "string") {
            errors.add(SemanticError(stmt.position, "Invalid type '${stmt.type}'"))
        }
        val value = stmt.value
        if (value != null && errors.isEmpty()) {
            val valueType = resolveType(value, environment)
            if (valueType != null && valueType != stmt.type) {
                errors.add(
                    SemanticError(
                        stmt.position,
                        "Cannot assign $valueType to ${stmt.type}"
                    )
                )
            }
        }
    }

    private fun assiCase(
        stmt: Assignment,
        errors: MutableList<SemanticError>,
        environment: Environment
    ) {
        val variable = environment.lookup(stmt.name)
        if (variable != null) {
            val valueType = resolveType(stmt.value, environment)
            if (valueType != null && valueType != variable.type) {
                errors.add(SemanticError(stmt.position, "Cannot assign $valueType to ${variable.type}"))
            }
        }

    }
}
