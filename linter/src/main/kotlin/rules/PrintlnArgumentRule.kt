package rules

import LintRule
import dataclass.LintNotification
import dataclass.Severity
import recurses.Identifier
import recurses.NumberLiteral
import recurses.PrintStatement
import recurses.Program
import recurses.StringLiteral


// --> Inspecciona los stmt y verifica que sea un identifier o literal
class PrintlnArgumentRule : LintRule {

    override val id: String = "println-argument"

    override fun check(program: Program): List<LintNotification> {
        val notifications = mutableListOf<LintNotification>()

        for (stmt in program.statements) {
            if (stmt is PrintStatement) {
                val argument = stmt.argument
                // --> Si no es un Identifier ni un Literal (osea, es una BinaryExpression)
                if (argument !is Identifier && argument !is NumberLiteral && argument !is StringLiteral) {
                    notifications.add(
                        LintNotification(
                            rule = id,
                            severity = Severity.WARNING,
                            message = "println argument must be an identifier or a literal, complex expressions are not allowed",
                            position = argument.position
                        )
                    )
                }
            }
        }

        return notifications
    }
}
