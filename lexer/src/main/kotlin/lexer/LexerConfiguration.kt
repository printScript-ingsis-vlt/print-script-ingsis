package lexer

import token.TokenType

data class LexerConfiguration(
    val keywords: Map<String, TokenType>,
    val operators: Map<String, TokenType>,
)
