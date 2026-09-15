package lexer

import ast.PrintScriptVersion
import ast.VersionConfigurationProvider
import token.TokenType

object LexerConfigurations : VersionConfigurationProvider<LexerConfiguration> {
    // let es la unica keyword que esta presente en todas las versiones
    private val baseKeywords =
        mapOf(
            "let" to TokenType.LET,
        )

    // Operaciones que se usan en todas las versiones
    private val baseOperators =
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
        )

    val v1_0 =
        LexerConfiguration(
            keywords = baseKeywords,
            operators = baseOperators,
        )

    val v1_1 =
        LexerConfiguration(
            keywords =
                baseKeywords +
                    mapOf(
                        "const" to TokenType.CONST,
                        "boolean" to TokenType.BOOLEAN,
                        "if" to TokenType.IF,
                        "else" to TokenType.ELSE,
                        "true" to TokenType.TRUE,
                        "false" to TokenType.FALSE,
                        "readInput" to TokenType.READINPUT,
                        "readEnv" to TokenType.READENV,
                    ),
            operators =
                baseOperators +
                    mapOf(
                        "{" to TokenType.LEFT_BRACE,
                        "}" to TokenType.RIGHT_BRACE,
                    ),
        )

    val default = v1_1

    override fun getConfiguration(version: PrintScriptVersion): LexerConfiguration =
        when (version) {
            PrintScriptVersion.V1_0 -> v1_0
            PrintScriptVersion.V1_1 -> v1_1
        }
}
