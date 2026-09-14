package semantic

import runtime.Environment

/**
 * Estado compartido durante el análisis semántico de un programa
 *
 * Por ahora contiene el entorno de símbolos existente. La idea es que el analyzer
 * cree un contexto unico por programa
 */
class SemanticContext(
    val environment: Environment = Environment(),
)
