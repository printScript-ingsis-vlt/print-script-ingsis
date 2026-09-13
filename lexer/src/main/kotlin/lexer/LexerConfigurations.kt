package lexer

import token.TokenType

object LexerConfigurations {
    val default =
        LexerConfiguration(
            keywords =
                mapOf(
                    "let" to TokenType.LET,
                ),
            operators =
                mapOf(
                    ":" to TokenType.COLON,
                    "=" to TokenType.EQUAL,
                    ";" to TokenType.SEMICOLON,
                    "+" to TokenType.PLUS,
                    "-" to TokenType.MINUS,
                    "*" to TokenType.STAR,
                    "/" to TokenType.SLASH,
                    "(" to TokenType.LEFT_PAREN,
                    ")" to TokenType.RIGHT_PAREN,
                ),
        )
}
