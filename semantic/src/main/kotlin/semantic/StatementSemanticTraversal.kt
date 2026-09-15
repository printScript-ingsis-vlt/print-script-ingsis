package semantic

import ast.Stmt
import result.SemanticError

/**
 * Recorre bloques de statements usando los handlers registrados para una versión.
 *
 * Los handlers compuestos, como el futuro if, usan este contrato para delegar el
 * análisis de sus statements hijos sin depender de [SemanticAnalyzer]
 */
interface StatementSemanticTraversal {
    // Valida cada statement en orden y actualiza el contexto recibido solo cuando
    // ese statement no tiene errores. El contexto puede ser una copia temporal
    fun validateAndUpdate(
        statements: List<Stmt>,
        context: SemanticContext,
    ): List<SemanticError>

    // Aplica únicamente las actualizaciones de statements ya validados
    fun update(
        statements: List<Stmt>,
        context: SemanticContext,
    )
}
