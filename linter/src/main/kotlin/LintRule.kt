import dataclass.LintNotification
import recurses.Program

interface LintRule {
    val id: String
    fun check(program: Program): List<LintNotification> // --> Usa los valores de LintConfig para validar
}
