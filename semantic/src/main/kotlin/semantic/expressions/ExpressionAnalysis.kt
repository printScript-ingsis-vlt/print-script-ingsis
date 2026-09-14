package semantic.expressions

import result.SemanticError

/**
 * Resultado inmutable del análisis semántico de una expresión
 */
data class ExpressionAnalysis(
    // Nulo cuando no puede inferirse un tipo
    val type: String?,
    val errors: List<SemanticError>,
    // Solo contiene un valor cuando la expresión puede calcularse estáticamente.
    // Permite ciertas validaciones sin depender de valores disponibles en runtime.
    val knownNumberValue: Double? = null,
)
