package formatter

import kotlinx.serialization.Serializable

/**
 * Define las reglas de formateo para PrintScript.
 *
 * REGLAS CONFIGURABLES (todas nullable a propósito):
 * - Espacio antes de ":" en declaraciones
 * - Espacio después de ":" en declaraciones
 * - Espacio antes y después de "=" en asignaciones
 * - Saltos de línea antes de "println" (0, 1 o 2)
 * - Espacios de indentación dentro de un bloque
 * - Espacio pegado a los paréntesis de "println" (ej. "println ( x );")
 * - Llave del "if" en la misma línea o en la línea de abajo
 *
 * `null` en cualquiera de estos campos significa "no forzar esta regla, preservar el
 * espaciado del archivo original" - a diferencia de `true`/`false`, que sí la fuerzan.
 *
 * REGLAS FIJAS (no configurables):
 * - Salto de línea después de ";"
 * - Máximo 1 espacio entre tokens
 * - Espacio antes y después de operadores (+, -, *, /)
 */
@Serializable
data class FormattingRules(
    val spaceBeforeColon: Boolean? = null,
    val spaceAfterColon: Boolean? = null,
    val spaceAroundEqual: Boolean? = null,
    val newlinesBeforePrintln: Int? = null,
    val indentationSpaces: Int? = null,
    val spaceAroundPrintParens: Boolean? = null,
    val ifBraceOnNewLine: Boolean? = null,
) {
    companion object {
        const val NEWLINE_AFTER_SEMICOLON = true
        const val MAX_SPACES_BETWEEN_TOKENS = 1
        const val SPACE_AROUND_OPERATORS = true

        // Fallbacks cuando la regla nullable viene en null (hasta que el formatter
        // pueda preservar el espaciado original de verdad)
        const val DEFAULT_INDENTATION_SPACES = 2
        const val DEFAULT_NEWLINES_BEFORE_PRINTLN = 1

        fun default() = FormattingRules()
    }
}
