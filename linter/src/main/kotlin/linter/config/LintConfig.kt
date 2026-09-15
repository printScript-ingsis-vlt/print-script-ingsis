package linter.config

import kotlinx.serialization.Serializable

@Serializable
// --> Desserializa las variables definidas en el JSON, definiendo que campos existes y que tipos tienen
data class LintConfig(
    // Default: camelCase identifiers.
    val identifierFormat: IdentifierFormat = IdentifierFormat.CAMEL_CASE,
    // Default: do not validate println arguments.
    val printlnArgumentCheck: Boolean = false,
    // Default: do not validate readInput arguments.
    val readInputArgumentCheck: Boolean = false,
)

enum class IdentifierFormat { CAMEL_CASE, SNAKE_CASE }
