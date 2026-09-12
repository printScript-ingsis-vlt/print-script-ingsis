package formatter

import kotlinx.serialization.Serializable

/**
 * Define las reglas de formateo para PrintScript.
 *
 * REGLAS CONFIGURABLES:
 * - Espacio antes de ":" en declaraciones
 * - Espacio después de ":" en declaraciones
 * - Espacio antes y después de "=" en asignaciones
 * - Saltos de línea antes de "println" (0, 1 o 2)
 *
 * REGLAS FIJAS (no configurables):
 * - Salto de línea después de ";"
 * - Máximo 1 espacio entre tokens
 * - Espacio antes y después de operadores (+, -, *, /)
 */
@Serializable
data class FormattingRules(
    val spaceBeforeColon: Boolean = true,
    val spaceAfterColon: Boolean = true,
    val spaceAroundEqual: Boolean = true,
    val newlinesBeforePrintln: Int = 1,
) {
    companion object {
        const val NEWLINE_AFTER_SEMICOLON = true
        const val MAX_SPACES_BETWEEN_TOKENS = 1
        const val SPACE_AROUND_OPERATORS = true

        fun default() = FormattingRules()
    }
}
