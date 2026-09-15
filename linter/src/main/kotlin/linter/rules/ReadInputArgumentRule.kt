package linter.rules

import ast.BooleanLiteral
import ast.Expr
import ast.Identifier
import ast.NumberLiteral
import ast.Program
import ast.ReadInputExpression
import ast.StringLiteral
import linter.AstTraversal
import linter.LintRule
import linter.dataclass.LintNotification
import linter.dataclass.Severity

// Verifica que cada llamada a readInput reciba un identificador o un literal como prompt
class ReadInputArgumentRule : LintRule {
    override val id: String = "read-input-argument"

    override fun check(program: Program): List<LintNotification> {
        val notifications = mutableListOf<LintNotification>()

        AstTraversal.forEachExpression(program) { expression ->
            if (expression is ReadInputExpression && !expression.prompt.isIdentifierOrLiteral()) {
                notifications.add(
                    LintNotification(
                        rule = id,
                        severity = Severity.ERROR,
                        message =
                            "readInput argument must be an identifier or a literal, " +
                                "complex expressions are not allowed",
                        position = expression.prompt.position,
                    ),
                )
            }
        }

        return notifications
    }

    private fun Expr.isIdentifierOrLiteral(): Boolean =
        this is Identifier ||
            this is NumberLiteral ||
            this is StringLiteral ||
            this is BooleanLiteral
}
