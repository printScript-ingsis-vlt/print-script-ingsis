package rules

import LintRule
import config.IdentifierFormat
import dataclass.LintNotification
import dataclass.Severity
import recurses.Assignment
import recurses.Program
import recurses.Stmt
import recurses.VariableDeclaration


// --> Rule que revisa todo identificador o asignacion que se le pase
class IdentifierFormatRule(
    private val format: IdentifierFormat
) : LintRule {

    override val id: String = "identifier-format"

    // --> Expresiones regulares según la convención
    private val camelCaseRegex = Regex("^[a-z][a-zA-Z0-9]*$")
    private val snakeCaseRegex = Regex("^[a-z][a-z0-9]*(_[a-z0-9]+)*$")

    override fun check(program: Program): List<LintNotification> {
        val notifications = mutableListOf<LintNotification>()

        for (stmt in program.statements) {
            when (stmt) {
                is VariableDeclaration -> {
                    if (!isValid(stmt.name)) {
                        notifications.add(
                            LintNotification(
                                rule = id,
                                severity = Severity.WARNING,
                                message = "Identifier '${stmt.name}' does not match $format naming convention",
                                position = stmt.position
                            )
                        )
                    }
                }

                is Assignment -> {
                    if (!isValid(stmt.name)) {
                        notifications.add(
                            LintNotification(
                                rule = id,
                                severity = Severity.WARNING,
                                message = "Identifier '${stmt.name}' does not match $format naming convention",
                                position = stmt.position
                            )
                        )
                    }
                }
                else -> { /* Otros statements no declaran identificadores */ }
            }
        }

        return notifications
    }

    private fun isValid(name: String): Boolean {
        return when (format) {
            IdentifierFormat.CAMEL_CASE -> camelCaseRegex.matches(name)
            IdentifierFormat.SNAKE_CASE -> snakeCaseRegex.matches(name)
        }
    }
}
