package linter

import ast.Program
import linter.config.LintConfig
import linter.dataclass.LintNotification
import linter.rules.IdentifierFormatRule
import linter.rules.PrintlnArgumentRule

/**
 * Clase encargada de recibir la config,
 * inicializar las reglas
 * y aplicarlas sobre el AST para notificar las alertas
 */

class PrintScriptLinter(private val config: LintConfig = LintConfig()) : Linter {
    // --> Las reglas se instancian basándose en la configuración provista de antes
    private val rules: List<LintRule> = buildRules()

    private fun buildRules(): List<LintRule> {
        val activeRules = mutableListOf<LintRule>()

        // --> Siempre se agrega la regla de formato con la convención indicada en la config
        activeRules.add(IdentifierFormatRule(config.identifierFormat))

        // --> Solo se agrega la regla de println si está habilitada en la config
        if (config.printlnArgumentCheck) {
            activeRules.add(PrintlnArgumentRule())
        }

        return activeRules
    }

    override fun lint(program: Program): List<LintNotification> {
        // --> Ejecuta todas las reglas activas sobre el AST del programa
        return rules.flatMap { it.check(program) }
    }
}
