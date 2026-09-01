import interfaces.Lexer
import recurses.Position
import recurses.Token
import recurses.TokenType
import result.LexicalError
import result.Result
import java.io.PushbackReader
import java.io.Reader
import java.io.StringReader

// lexer que lee de un java.io.Reader caracter por caracter, sin volcar la fuente completa a memoria
class StreamLexer(
    reader: Reader,
    private val keywords: Map<String, TokenType> = DEFAULT_KEYWORDS,
    private val singleCharTokens: Map<Char, TokenType> = DEFAULT_SINGLE_CHAR_TOKENS,
) : Lexer {
    // pushbackReader(1) da un "peek" de un caracter sin consumirlo
    private val input = PushbackReader(reader, 1)

    private var line = 1
    private var column = 1

    // flag que indica cuadno se devuelve el token de fin de archivo
    private var emittedEof = false

    companion object {
        fun fromString(source: String): StreamLexer = StreamLexer(StringReader(source))

        fun tokenize(reader: Reader): Result<List<Token>, LexicalError> {
            val lexer = StreamLexer(reader)
            val tokens = mutableListOf<Token>()
            while (lexer.hasNext()) {
                when (val result = lexer.nextToken()) {
                    is Result.Success -> tokens.add(result.value)
                    is Result.Failure -> return Result.Failure(result.error)
                }
            }
            return Result.Success(tokens)
        }

        fun tokenize(source: String): Result<List<Token>, LexicalError> {
            return tokenize(StringReader(source))
        }

        private val DEFAULT_KEYWORDS =
            mapOf(
                "let" to TokenType.LET,
            )

        private val DEFAULT_SINGLE_CHAR_TOKENS =
            mapOf(
                ':' to TokenType.COLON,
                '=' to TokenType.EQUAL,
                ';' to TokenType.SEMICOLON,
                '+' to TokenType.PLUS,
                '-' to TokenType.MINUS,
                '*' to TokenType.STAR,
                '/' to TokenType.SLASH,
                '(' to TokenType.LEFT_PAREN,
                ')' to TokenType.RIGHT_PAREN,
            )
    }

    override fun hasNext(): Boolean = !emittedEof

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

    // despachador: mira el primer char y decide a donde mandarlo
    override fun nextToken(): Result<Token, LexicalError> {
        if (emittedEof) throw NoSuchElementException("El lexer ya devolvió EOF")

        skipWhitespace()

        val startPos = Position(line, column)
        // lee un char, si no hay más corta devolviendo EOF
        val c = readChar() ?: return Result.Success(eof(startPos))

        // si el char esta en el dic arma el token
        return when {
            c in singleCharTokens -> {
                val token = Token(singleCharTokens[c]!!, c.toString(), startPos, Position(line, column))
                Result.Success(token)
            }
            c == '"' || c == '\'' -> readString(c, startPos)
            c.isDigit() -> Result.Success(readNumber(c, startPos))
            c.isLetter() || c == '_' -> Result.Success(readIdentifierOrKeyword(c, startPos))
            else -> Result.Failure(LexicalError(startPos, "Caracter inesperado '$c'"))
        }
    }

    // funciones de bajo nivel del stream

    // lee un caracter del stream y actualiza línea/columna. null si es EOF.
    private fun readChar(): Char? {
        val code = input.read()
        if (code == -1) return null
        val c = code.toChar()
        if (c == '\n') {
            line++
            column = 1
        } else {
            column++
        }
        return c
    }

    // mira el próximo caracter sin consumirlo (lo devuelve al buffer). null si es EOF.
    private fun peekChar(): Char? {
        val code = input.read()
        if (code == -1) return null
        input.unread(code)
        return code.toChar()
    }

    private fun eof(pos: Position): Token {
        emittedEof = true
        return Token(TokenType.EOF, "", pos, pos)
    }

    // si es espacio/tab lo consume
    private fun skipWhitespace() {
        while (true) {
            val c = peekChar() ?: return
            if (c !in " \t\r\n") return
            readChar()
        }
    }

    //  reglas léxicas

    // única regla léxica que puede fallar (falta la comilla de cierre)
    private fun readString(
        quote: Char,
        startPos: Position,
    ): Result<Token, LexicalError> {
        val sb = StringBuilder()
        var result: Result<Token, LexicalError>? = null
        while (result == null) {
            val c = readChar()
            result = when {
                c == null -> Result.Failure(LexicalError(startPos, "String sin cerrar"))
                c == quote -> {
                    val token = Token(TokenType.STRING_LITERAL, sb.toString(), startPos, Position(line, column))
                    Result.Success(token)
                }
                c == '\n' -> Result.Failure(LexicalError(startPos, "String sin cerrar antes de fin de línea"))
                else -> {
                    sb.append(c)
                    null
                }
            }
        }
        return result
    }

    private fun readNumber(
        first: Char,
        startPos: Position,
    ): Token {
        val sb = StringBuilder().append(first)
        var sawDot = false
        while (true) {
            val c = peekChar() ?: break
            when {
                c.isDigit() -> sb.append(readChar())
                c == '.' && !sawDot -> {
                    sawDot = true
                    sb.append(readChar())
                }
                else -> return Token(TokenType.NUMBER_LITERAL, sb.toString(), startPos, Position(line, column))
            }
        }
        return Token(TokenType.NUMBER_LITERAL, sb.toString(), startPos, Position(line, column))
    }

    private fun readIdentifierOrKeyword(
        first: Char,
        startPos: Position,
    ): Token {
        val sb = StringBuilder().append(first)
        while (true) {
            val c = peekChar()
            if (c == null || (!c.isLetterOrDigit() && c != '_')) break
            sb.append(readChar())
        }
        val text = sb.toString()
        val type = keywords[text] ?: TokenType.IDENTIFIER
        return Token(type, text, startPos, Position(line, column))
    }
}
