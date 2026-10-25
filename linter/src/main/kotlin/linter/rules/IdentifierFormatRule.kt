package linter.rules

import ast.Program
import linter.LintRule
import linter.LinterConfiguration
import linter.LinterConfigurations
import linter.config.IdentifierFormat
import linter.dataclass.LintNotification
import linter.dataclass.Severity

// --> Rule que revisa todo identificador o asignacion que se le pase
class IdentifierFormatRule(
    private val format: IdentifierFormat,
    private val configuration: LinterConfiguration = LinterConfigurations.default,
) : LintRule {
    override val id: String = "identifier-format"

    // --> Expresiones regulares según la convención
    private val camelCaseRegex = Regex("^[a-z][a-zA-Z0-9]*$")
    private val snakeCaseRegex = Regex("^[a-z][a-z0-9]*(_[a-z0-9]+)*$")

    override fun check(program: Program): List<LintNotification> {
        val notifications = mutableListOf<LintNotification>()

        for (stmt in program.statements) {
            val name = configuration.statementHandler(stmt).identifier(stmt)
            if (name != null && !isValid(name)) {
                notifications.add(
                    LintNotification(
                        rule = id,
                        severity = Severity.WARNING,
                        message = "Identifier '$name' does not match $format naming convention",
                        position = stmt.position,
                    ),
                )
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
