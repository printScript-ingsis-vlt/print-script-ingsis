import interfaces.Lexer
import matcherImplementations.IdentifierMatcher
import matcherImplementations.NumberMatcher
import matcherImplementations.OperatorMatcher
import matcherImplementations.StringMatcher
import recurses.Position
import recurses.Token
import recurses.TokenType
import result.LexicalError
import result.Result
import java.io.Reader
import java.io.StringReader

class StreamLexer(
    reader: Reader,
    private val matchers: List<TokenMatcher> = defaultMatchers(DEFAULT_CONFIGURATION),
) : Lexer {
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

        if (first == null) {
            return Result.Success(eof(start)) // --> EOF si el caracter leido es null
        }

        val candidates = matchers.filter { matcher ->
            matcher.canStartWith(first) // --> Recorre todos los matchers
        // consultando cual acepta el caracter
        }

        val matcher =
            when (candidates.size) {
                0 -> {
                    return Result.Failure( // --> Ningun matcher lo maneja
                        LexicalError(start, "Caracter inesperado '$first'"),
                    )
                }

                1 -> candidates.single()

                else -> {
                    error( // --> Muchos matchers lo manejan
                        "Configuración ambigua: " +
                            candidates.joinToString { it.name } +
                            " aceptan el carácter '$first'",
                    )
                }
            }

        return matcher.match(cursor, start)
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

        fun defaultMatchers(
            configuration: LexerConfiguration,
        ): List<TokenMatcher> =
            listOf(
                StringMatcher(),
                NumberMatcher(),
                IdentifierMatcher(configuration.keywords), // --> Inyeccion de identifiers al matcher
                OperatorMatcher(configuration.operators), // --> Inyeccion de operadores al matcher
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
        ): Result<List<Token>, LexicalError> =
            StreamLexer(reader, defaultMatchers(configuration)).tokenize()

        fun tokenize(
            source: String,
            configuration: LexerConfiguration = DEFAULT_CONFIGURATION,
        ): Result<List<Token>, LexicalError> =
            tokenize(StringReader(source), configuration)
    }
}
