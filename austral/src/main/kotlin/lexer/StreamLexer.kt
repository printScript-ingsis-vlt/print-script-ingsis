package austral.src.main.kotlin.lexer

import austral.src.main.kotlin.commun.Lexer
import austral.src.main.kotlin.commun.ast.Position
import austral.src.main.kotlin.commun.Token
import austral.src.main.kotlin.commun.TokenType
import austral.src.main.kotlin.commun.result.LexicalError
import austral.src.main.kotlin.commun.result.Result
import java.io.PushbackReader
import java.io.Reader
import java.io.StringReader

// lexer que lee de un java.io.Reader caracter por caracter, sin volcar la fuente completa a memoria
class StreamLexer(reader: Reader) : Lexer {

    // pushbackReader(1) da un "peek" de un caracter sin consumirlo
    private val input = PushbackReader(reader, 1)

    private var line = 1
    private var column = 1
    // flag que indica cuadno se devuelve el token de fin de archivo
    private var emittedEof = false

    companion object {
        fun fromString(source: String): StreamLexer = StreamLexer(StringReader(source))

        private val KEYWORDS = mapOf(
            "let" to TokenType.LET
        )

        private val SINGLE_CHAR_TOKENS = mapOf(
            ':' to TokenType.COLON,
            '=' to TokenType.EQUAL,
            ';' to TokenType.SEMICOLON,
            '+' to TokenType.PLUS,
            '-' to TokenType.MINUS,
            '*' to TokenType.STAR,
            '/' to TokenType.SLASH,
            '(' to TokenType.LEFT_PAREN,
            ')' to TokenType.RIGHT_PAREN
        )
    }

    override fun hasNext(): Boolean = !emittedEof

    // despachador: mira el primer char y decide a donde mandarlo
    override fun nextToken(): Result<Token, LexicalError> {
        if (emittedEof) throw NoSuchElementException("El lexer ya devolvió EOF")

        skipWhitespace()

        val startPos = currentPosition()
        // lee un char, si no hay más corta devolviendo EOF
        val c = readChar() ?: return Result.Success(eof(startPos))

        // si el char esta en el dic arma el token
        SINGLE_CHAR_TOKENS[c]?.let { type ->
            return Result.Success(Token(type, c.toString(), startPos, currentPosition()))
        }

        return when {
            c == '"' || c == '\'' -> readString(c, startPos)
            c.isDigit() -> Result.Success(readNumber(c, startPos))
            c.isLetter() || c == '_' -> Result.Success(readIdentifierOrKeyword(c, startPos))
            else -> Result.Failure(LexicalError(startPos, "Caracter inesperado '$c'"))
        }
    }

    // funciones de bajo nivel del stream

    private fun currentPosition(): Position = Position(line, column)

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
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                readChar()
            } else {
                return
            }
        }
    }

    //  reglas léxicas

    // única regla léxica que puede fallar (falta la comilla de cierre)
    private fun readString(quote: Char, startPos: Position): Result<Token, LexicalError> {
        val sb = StringBuilder()
        while (true) {
            val c = readChar()
                ?: return Result.Failure(LexicalError(startPos, "String sin cerrar"))
            if (c == quote) break
            if (c == '\n') {
                return Result.Failure(LexicalError(startPos, "String sin cerrar antes de fin de línea"))
            }
            sb.append(c)
        }
        return Result.Success(Token(TokenType.STRING_LITERAL, sb.toString(), startPos, currentPosition()))
    }

    private fun readNumber(first: Char, startPos: Position): Token {
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
                else -> return Token(TokenType.NUMBER_LITERAL, sb.toString(), startPos, currentPosition())
            }
        }
        return Token(TokenType.NUMBER_LITERAL, sb.toString(), startPos, currentPosition())
    }

    private fun readIdentifierOrKeyword(first: Char, startPos: Position): Token {
        val sb = StringBuilder().append(first)
        while (true) {
            val c = peekChar() ?: break
            if (c.isLetterOrDigit() || c == '_') {
                sb.append(readChar())
            } else {
                break
            }
        }
        val text = sb.toString()
        val type = KEYWORDS[text] ?: TokenType.IDENTIFIER
        return Token(type, text, startPos, currentPosition())
    }
}