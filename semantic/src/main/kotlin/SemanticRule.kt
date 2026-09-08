import recurses.Environment
import recurses.Stmt
import result.SemanticError

interface SemanticRule {
    fun check(
        stmt: Stmt,
        environment: Environment,
    ): List<SemanticError>
} // --> La idea es poder ir agregando reglas sin tocar el analyzer
