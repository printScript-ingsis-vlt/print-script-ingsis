package semantic.symbols

/**
 * Información que el análisis semántico conoce de un identificador.
 *
 * No representa el valor que tendrá la variable durante la ejecución. Ese es
 * trabajo del Environment del interpreter. [knownNumberValue] solo conserva una
 * constante numérica conocida estáticamente para validaciones como división por
 * cero.
 */
data class SemanticSymbol(
    val type: String,
    val initialized: Boolean,
    val mutable: Boolean = true,
    val knownNumberValue: Double? = null,
)
