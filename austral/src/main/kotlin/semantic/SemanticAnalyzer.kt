package austral.src.main.kotlin.semantic

import austral.src.main.kotlin.commun.*
import austral.src.main.kotlin.commun.ast.BinaryExpression
import austral.src.main.kotlin.commun.ast.Expr
import austral.src.main.kotlin.commun.ast.Identifier
import austral.src.main.kotlin.commun.ast.NumberLiteral
import austral.src.main.kotlin.commun.ast.Position
import austral.src.main.kotlin.commun.ast.Program
import austral.src.main.kotlin.commun.ast.StringLiteral
import austral.src.main.kotlin.commun.ast.VariableDeclaration
import austral.src.main.kotlin.commun.result.SemanticError

// --> Hace todas las validaciones semanticas, construyendo el environment
class SemanticAnalyzer(private val rules: List<SemanticRule>) {

    fun analyze(program: Program): List<SemanticError> {
        val environment = Environment()
        val errors = mutableListOf<SemanticError>()

        for (stmt in program.statements) {
            errors.addAll(rules.flatMap { it.check(stmt, environment) })

            if (stmt is VariableDeclaration && !hasError(stmt.position, errors)) {
                val resolvedType = resolveType(stmt.value, environment)
                if (resolvedType != null) {
                    environment.declare(stmt.name, Variable(stmt.type, null))
                }
            }
        }

        return errors
    }

    private fun hasError(position: Position, errors: List<SemanticError>): Boolean =
        errors.any { it.position == position }

    private fun resolveType(expr: Expr?, env: Environment): String? = when (expr) {
        null -> null
        is NumberLiteral -> "number"
        is StringLiteral -> "string"
        is Identifier -> env.lookup(expr.name)?.type
        is BinaryExpression -> {
            val left = resolveType(expr.left, env)
            val right = resolveType(expr.right, env)
            if (left == null || right == null) null
            else if (expr.operator == "+" && (left == "string" || right == "string")) "string"
            else if (left == "number" && right == "number") "number"
            else null
        }
    }
}