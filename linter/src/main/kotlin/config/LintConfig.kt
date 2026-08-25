package config

import kotlinx.serialization.Serializable

@Serializable
// --> Desserializa las variables definidas en el JSON, definiendo que campos existes y que tipos tienen
data class LintConfig(
    val identifierFormat: IdentifierFormat = IdentifierFormat.CAMEL_CASE, // default
    val printlnArgumentCheck: Boolean = false, // default
)

enum class IdentifierFormat { CAMEL_CASE, SNAKE_CASE }
