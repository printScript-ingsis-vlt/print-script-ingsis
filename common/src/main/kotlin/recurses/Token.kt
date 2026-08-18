package recurses

enum class TokenType {
    LET,
    IDENTIFIER,
    NUMBER,
    STRING,
    LITERAL,
    ASSIGNMENT,
    EOF // end of file
    ,
    NUMBER_LITERAL,
    STRING_LITERAL,
    COLON,
    EQUAL,
    SEMICOLON,
    PLUS,
    MINUS,
    STAR,
    SLASH,
    LEFT_PAREN,
    RIGHT_PAREN
}


data class Token(
    val type: TokenType,
    val value: String,
    val start: Position,
    val end: Position
)
