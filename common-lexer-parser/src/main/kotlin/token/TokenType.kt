package token

enum class TokenType {
    LET,
    CONST,
    BOOLEAN,
    IF,
    ELSE,
    TRUE,
    FALSE,
    READINPUT,
    READENV,
    IDENTIFIER,
    EOF, // end of file
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
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
}
