package parser.stream

import ast.Position
import parser.engine.TokenCursor
import result.SyntaxError

/** Secuencia: A then B then C */
data class Seq(val rules: List<StreamRule>) : StreamRule {
    override fun parse(cursor: TokenCursor): StreamParseResult {
        val values = mutableListOf<Any?>()
        for (rule in rules) {
            when (val result = rule.parse(cursor)) {
                is StreamParseResult.Success -> values.add(result.value)
                is StreamParseResult.Failure -> return result
            }
        }
        return StreamParseResult.Success(values)
    }
}

/** Alternativa: A or B or C (con backtracking) */
data class Choice(val alternatives: List<StreamRule>) : StreamRule {
    override fun parse(cursor: TokenCursor): StreamParseResult {
        var lastError: SyntaxError? = null
        val mark = cursor.mark()

        for (alt in alternatives) {
            when (val result = alt.parse(cursor)) {
                is StreamParseResult.Success -> {
                    cursor.release(mark)
                    return result
                }
                is StreamParseResult.Failure -> {
                    lastError = result.error
                    cursor.reset(mark)
                }
            }
        }

        cursor.release(mark)
        return StreamParseResult.Failure(
            lastError
                ?: SyntaxError(
                    cursor.peek()?.start ?: Position(0, 0),
                    "No alternative matched",
                ),
        )
    }
}

/** Cero o más (con backtracking por intento) */
data class Many(val rule: StreamRule) : StreamRule {
    override fun parse(cursor: TokenCursor): StreamParseResult {
        val values = mutableListOf<Any?>()
        while (true) {
            val mark = cursor.mark()
            when (val result = rule.parse(cursor)) {
                is StreamParseResult.Success -> {
                    cursor.release(mark)
                    values.add(result.value)
                }
                is StreamParseResult.Failure -> {
                    cursor.reset(mark)
                    cursor.release(mark)
                    break
                }
            }
        }
        return StreamParseResult.Success(values)
    }
}

/** Opcional */
data class Opt(val rule: StreamRule) : StreamRule {
    override fun parse(cursor: TokenCursor): StreamParseResult {
        val mark = cursor.mark()
        return when (val result = rule.parse(cursor)) {
            is StreamParseResult.Success -> {
                cursor.release(mark)
                result
            }
            is StreamParseResult.Failure -> {
                cursor.reset(mark)
                cursor.release(mark)
                StreamParseResult.Success(null)
            }
        }
    }
}

/** Acción: transforma el resultado de una regla en un nodo del AST */
data class Action(
    val rule: StreamRule,
    val transform: (Any?) -> Any?,
) : StreamRule {
    override fun parse(cursor: TokenCursor): StreamParseResult =
        when (val result = rule.parse(cursor)) {
            is StreamParseResult.Success ->
                StreamParseResult.Success(transform(result.value))
            is StreamParseResult.Failure -> result
        }
}

/** Referencia perezosa, para reglas recursivas */
data class Ref(private val resolve: () -> StreamRule) : StreamRule {
    override fun parse(cursor: TokenCursor): StreamParseResult = resolve().parse(cursor)
}
