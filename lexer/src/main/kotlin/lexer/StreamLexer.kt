package lexer

import ast.Position
import result.LexicalError
import result.Result
import token.Token
import token.TokenType
import java.io.Reader
import java.io.StringReader

class StreamLexer(
    reader: Reader,
    private val matchers: List<TokenMatcher> = LexerMatcherFactory.create(LexerConfigurations.default),
) : Lexer {
    constructor(
        reader: Reader,
        keywords: Map<String, TokenType>,
        singleCharTokens: Map<Char, TokenType>,
    ) : this(
        reader = reader,
        matchers =
            LexerMatcherFactory.create(
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

    companion object {
        // Controla lectura token a token (let x -> LET, IDENTIFIER)
        fun fromString(
            source: String,
            configuration: LexerConfiguration = LexerConfigurations.default,
        ): StreamLexer =
            StreamLexer(
                reader = StringReader(source),
                matchers = LexerMatcherFactory.create(configuration),
            )

        // Tokeniza un archivo/stream completo
        fun tokenize(
            reader: Reader,
            configuration: LexerConfiguration = LexerConfigurations.default,
        ): Result<List<Token>, LexicalError> = StreamLexer(reader, LexerMatcherFactory.create(configuration)).tokenize()

        // Tokeniza un string completo
        fun tokenize(
            source: String,
            configuration: LexerConfiguration = LexerConfigurations.default,
        ): Result<List<Token>, LexicalError> = tokenize(StringReader(source), configuration)
    }
}
