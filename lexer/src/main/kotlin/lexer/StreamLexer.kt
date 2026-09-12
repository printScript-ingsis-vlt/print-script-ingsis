package lexer

import ast.Position
import lexer.implementations.IdentifierMatcher
import lexer.implementations.NumberMatcher
import lexer.implementations.OperatorMatcher
import lexer.implementations.StringMatcher
import result.LexicalError
import result.Result
import token.Token
import token.TokenType
import java.io.Reader
import java.io.StringReader

class StreamLexer(
    reader: Reader,
    private val matchers: List<TokenMatcher> = defaultMatchers(DEFAULT_CONFIGURATION),
) : Lexer {
    constructor(
        reader: Reader,
        keywords: Map<String, TokenType>,
        singleCharTokens: Map<Char, TokenType>,
    ) : this(
        reader = reader,
        matchers =
            defaultMatchers(
                LexerConfiguration(
                    keywords = keywords,
                    operators = singleCharTokens.mapKeys { (character, _) -> character.toString() },
                ),
            ),
    )

    private val cursor = LexerCursor(reader)

    private var emittedEof = false

    init {
        require(matchers.isNotEmpty()) {
            "El lexer necesita al menos un TokenMatcher"
        }

        require(matchers.map { it.name }.distinct().size == matchers.size) {
            "No puede haber matchers con el mismo nombre"
        }
    }

    override fun hasNext(): Boolean = !emittedEof

    override fun nextToken(): Result<Token, LexicalError> {
        if (emittedEof) {
            throw NoSuchElementException("El lexer ya devolvió EOF")
        }

        cursor.skipWhitespace() // --> No leer espacios vacios

        val start = cursor.position
        val first = cursor.peek()

        val result =
            if (first == null) {
                Result.Success(eof(start)) // --> EOF si el caracter leido es null
            } else {
                val candidates =
                    matchers.filter { matcher ->
                        matcher.canStartWith(first) // --> Recorre todos los matchers
                        // consultando cual acepta el caracter
                    }

                when (candidates.size) {
                    0 ->
                        // Ningún matcher maneja el carácter.
                        Result.Failure(
                            LexicalError(start, "Caracter inesperado '$first'"),
                        )

                    1 -> candidates.single().match(cursor, start)

                    else -> {
                        // Más de un matcher maneja el carácter.
                        error(
                            "Configuración ambigua: " +
                                candidates.joinToString { it.name } +
                                " aceptan el carácter '$first'",
                        )
                    }
                }
            }

        return result
    }

    override fun tokenize(): Result<List<Token>, LexicalError> {
        val tokens = mutableListOf<Token>()

        while (hasNext()) {
            when (val result = nextToken()) {
                is Result.Success -> tokens.add(result.value)
                is Result.Failure -> return Result.Failure(result.error)
            }
        }

        return Result.Success(tokens)
    }

    private fun eof(position: Position): Token {
        emittedEof = true

        return Token(
            type = TokenType.EOF,
            value = "",
            start = position,
            end = position,
        )
    }

    companion object { // --> La configuracion default con la cual se inicializa el lexer
        private val DEFAULT_CONFIGURATION =
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

        fun defaultMatchers(configuration: LexerConfiguration): List<TokenMatcher> =
            listOf(
                StringMatcher(),
                NumberMatcher(),
                // Inyección de keywords al matcher de identificadores.
                IdentifierMatcher(configuration.keywords),
                // Inyección de operadores al matcher correspondiente.
                OperatorMatcher(configuration.operators),
            )

        fun fromString(
            source: String,
            configuration: LexerConfiguration = DEFAULT_CONFIGURATION,
        ): StreamLexer =
            StreamLexer(
                reader = StringReader(source),
                matchers = defaultMatchers(configuration),
            )

        fun tokenize(
            reader: Reader,
            configuration: LexerConfiguration = DEFAULT_CONFIGURATION,
        ): Result<List<Token>, LexicalError> = StreamLexer(reader, defaultMatchers(configuration)).tokenize()

        fun tokenize(
            source: String,
            configuration: LexerConfiguration = DEFAULT_CONFIGURATION,
        ): Result<List<Token>, LexicalError> = tokenize(StringReader(source), configuration)
    }
}
