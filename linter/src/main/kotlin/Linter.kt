import dataclass.LintNotification
import recurses.Program

interface Linter {
    fun lint(program: Program): List<LintNotification>
}
