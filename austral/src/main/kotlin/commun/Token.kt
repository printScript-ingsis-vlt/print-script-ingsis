package austral.src.main.kotlin.commun

enum class TokenType {
    LET,
    IDENTIFIER,
    NUMBER,
    STRING,
    LITERAL,
    ASSIGNMENT,
    EOF // end of file
}

data class Position(
    val line: Int,
    val column: Int
)

data class Token(
    val type: TokenType,
    val value: String,
    val start: Position,
    val end: Position
)
