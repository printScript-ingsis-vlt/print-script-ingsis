package semantic.expressions

import result.SemanticError

/**
 * Resultado inmutable del análisis semántico de una expresión
 */
data class ExpressionAnalysis(

    // Nulo cuando no puede inferirse un tipo
    val type: String?,
    val errors: List<SemanticError>,
    // Solo contiene un valor cuando la expresion se puede calcular de manera estatica, permitiendo ciertas validaciones no en runtime
    val knownNumberValue: Double? = null,
)
