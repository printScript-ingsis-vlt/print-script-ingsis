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

class DeclarationValidator : SemanticRule { // --> Valida los tipos y que el valor declarado coincida con el tipo

    override fun check(stmt: Stmt, environment: Environment): List<SemanticError> {
        val errors = mutableListOf<SemanticError>()
        when (stmt) {
            is VariableDeclaration -> {
                if (stmt.type != "number" && stmt.type != "string") {
                    errors.add(SemanticError(stmt.position, "Invalid type '${stmt.type}'"))
                }
                if (stmt.value != null && errors.isEmpty()) {
                    val valueType = resolveType(stmt.value, environment)
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
}