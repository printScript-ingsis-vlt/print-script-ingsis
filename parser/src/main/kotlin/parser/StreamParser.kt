package parser

import ast.Position
import ast.Program
import ast.Stmt
import parser.engine.ListTokenCursor
import parser.engine.TokenCursor
import parser.stream.Choice
import parser.stream.StreamParseResult
import parser.stream.StreamRule
import result.Result
import result.SyntaxError
import token.Token
import token.TokenType

/**
 * Parser streaming. Equivalente a [ConfigurableParser], pero trabaja
 * sobre un [TokenCursor] y puede emitir statements de a uno sin
 * materializar el Program completo.
 *
 * Convive con el parser clásico: no lo reemplaza.
 */
class StreamParser(
    private val statement: StreamRule,
) {
    /**
     * API principal: parsea statement por statement.
     * Ideal para archivos grandes (memoria constante por statement).
     */
    fun parseStatements(cursor: TokenCursor): Sequence<Result<Stmt, SyntaxError>> =
        sequence {
            while (true) {
                val next = cursor.peek()
                if (next == null || next.type == TokenType.EOF) break

                when (val result = statement.parse(cursor)) {
                    is StreamParseResult.Success -> {
                        yield(Result.Success(result.value as Stmt))
                    }
                    is StreamParseResult.Failure -> {
                        yield(Result.Failure(result.error))
                        recover(cursor)
                    }
                }
            }
        }

    /**
     * Compatibilidad: recibe List completa y arma un Program.
     * Útil para tests y para no romper callers que esperan Program.
     */
    fun parse(tokens: List<Token>): Result<Program, List<SyntaxError>> {
        val cursor = ListTokenCursor(tokens)
        val statements = mutableListOf<Stmt>()
        val errors = mutableListOf<SyntaxError>()

        parseStatements(cursor).forEach { result ->
            when (result) {
                is Result.Success -> statements.add(result.value)
                is Result.Failure -> errors.add(result.error)
            }
        }

        return if (errors.isEmpty()) {
            Result.Success(
                Program(
                    position = tokens.firstOrNull()?.start ?: Position(0, 0),
                    statements = statements,
                ),
            )
        } else {
            Result.Failure(errors)
        }
    }

    /**
     * Parsea desde un cursor arbitrario (BufferedTokenCursor del lexer, etc.)
     * y materializa el Program. Usar solo si el AST completo cabe en memoria.
     */
    fun parse(cursor: TokenCursor): Result<Program, List<SyntaxError>> {
        val statements = mutableListOf<Stmt>()
        val errors = mutableListOf<SyntaxError>()
        var firstPosition: Position? = null

        parseStatements(cursor).forEach { result ->
            when (result) {
                is Result.Success -> {
                    if (firstPosition == null) firstPosition = result.value.position
                    statements.add(result.value)
                }
                is Result.Failure -> errors.add(result.error)
            }
        }

        return if (errors.isEmpty()) {
            Result.Success(
                Program(
                    position = firstPosition ?: Position(0, 0),
                    statements = statements,
                ),
            )
        } else {
            Result.Failure(errors)
        }
    }

    /** Error recovery: avanza hasta ';' o EOF (igual que el parser clásico). */
    private fun recover(cursor: TokenCursor) {
        while (true) {
            val t = cursor.peek() ?: break
            if (t.type == TokenType.SEMICOLON || t.type == TokenType.EOF) {
                if (t.type == TokenType.SEMICOLON) cursor.advance()
                break
            }
            cursor.advance()
        }
    }
}

/**
 * Factory cómoda: arma el StreamParser a partir de una lista de reglas
 * de statement (misma idea que ConfigurableParser + GrammarConfiguration).
 */
fun streamParserOf(statementRules: List<StreamRule>): StreamParser =
    StreamParser(statement = Choice(statementRules))
