package austral.src.main.kotlin.semantic

import austral.src.main.kotlin.commun.ast.Stmt
import austral.src.main.kotlin.commun.result.SemanticError
import austral.src.main.kotlin.commun.Environment

interface SemanticRule {
    fun check(stmt: Stmt, environment: Environment): List<SemanticError>
} // --> La idea es poder ir agregando reglas sin tocar el analyzer