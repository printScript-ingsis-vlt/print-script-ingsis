package parser.grammar

import parser.engine.token
import token.TokenType

/** terminales reutilizados por varias reglas de statement/expression */
object Terminals {
    val LET = token(TokenType.LET, "let")
    val COLON = token(TokenType.COLON, ":")
    val EQUAL = token(TokenType.EQUAL, "=")
    val SEMICOLON = token(TokenType.SEMICOLON, ";")
    val IDENTIFIER = token(TokenType.IDENTIFIER, "identifier")
    val LEFT_PAREN = token(TokenType.LEFT_PAREN, "(")
    val RIGHT_PAREN = token(TokenType.RIGHT_PAREN, ")")
    val BOOLEAN = token(TokenType.BOOLEAN, "boolean")
}
