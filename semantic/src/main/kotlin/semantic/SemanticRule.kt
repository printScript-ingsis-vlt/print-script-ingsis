package semantic

import ast.Stmt
import result.SemanticError
import runtime.Environment

interface SemanticRule {
    fun check(
        stmt: Stmt,
        environment: Environment,
    ): List<SemanticError>
} // --> La idea es poder ir agregando reglas sin tocar el analyzer
