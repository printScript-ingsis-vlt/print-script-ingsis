package linter

import ast.Program
import linter.dataclass.LintNotification

interface LintRule {
    val id: String

    fun check(program: Program): List<LintNotification> // --> Usa los valores de LintConfig para validar
}
