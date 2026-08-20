package engine

import common.src.main.kotlin.result.SyntaxError
import recurses.Position
import recurses.Token
import recurses.TokenType

sealed interface ParseResult {
    data class Success(val value: Any?, val next: Int) : ParseResult
    data class Failure(val error: SyntaxError) : ParseResult
}

// ------------------------------------------------------------
// Reglas
// ------------------------------------------------------------
sealed interface Rule {
    fun parse(tokens: List<Token>, pos: Int): ParseResult
}

/** Secuencia: A then B then C */
data class Seq(val rules: List<Rule>) : Rule {
    override fun parse(tokens: List<Token>, pos: Int): ParseResult {
        val values = mutableListOf<Any?>()
        var current = pos

        for (rule in rules) {
            when (val result = rule.parse(tokens, current)) {
                is ParseResult.Success -> {
                    values.add(result.value)
                    current = result.next
                }
                is ParseResult.Failure -> return result
            }
        }
        return ParseResult.Success(values, current)
    }
}

/** Alternativa: A or B or C */
data class Choice(val alternatives: List<Rule>) : Rule {
    override fun parse(tokens: List<Token>, pos: Int): ParseResult {
        var lastError: SyntaxError? = null

        for (alt in alternatives) {
            when (val result = alt.parse(tokens, pos)) {
                is ParseResult.Success -> return result
                is ParseResult.Failure -> lastError = result.error
            }
        }
        return ParseResult.Failure(
            lastError ?: SyntaxError(tokens.getOrNull(pos)?.start ?: Position(0, 0), "No alternative matched")
        )
    }
}

/** Cero o más */
data class Many(val rule: Rule) : Rule {
    override fun parse(tokens: List<Token>, pos: Int): ParseResult {
        val values = mutableListOf<Any?>()
        var current = pos

        while (true) {
            when (val result = rule.parse(tokens, current)) {
                is ParseResult.Success -> {
                    values.add(result.value)
                    current = result.next
                }
                is ParseResult.Failure -> break
            }
        }
        return ParseResult.Success(values, current)
    }
}

/** Opcional */
data class Opt(val rule: Rule) : Rule {
    override fun parse(tokens: List<Token>, pos: Int): ParseResult {
        return when (val result = rule.parse(tokens, pos)) {
            is ParseResult.Success -> result
            is ParseResult.Failure -> ParseResult.Success(null, pos) // no consumió nada
        }
    }
}

/** Terminal por TokenType */
data class TokenRule(val type: TokenType, val expected: String = type.name) : Rule {
    override fun parse(tokens: List<Token>, pos: Int): ParseResult {
        if (pos >= tokens.size) {
            return ParseResult.Failure(SyntaxError(Position(0, 0), "Unexpected end of input, expected $expected"))
        }
        val token = tokens[pos]
        return if (token.type == type) {
            ParseResult.Success(token, pos + 1)
        } else {
            ParseResult.Failure(SyntaxError(token.start, "Expected $expected, got '${token.value}'"))
        }
    }
}

/** Acción: transforma el resultado de una regla en un nodo del AST */
data class Action(val rule: Rule, val transform: (Any?) -> Any?) : Rule {
    override fun parse(tokens: List<Token>, pos: Int): ParseResult {
        return when (val result = rule.parse(tokens, pos)) {
            is ParseResult.Success -> ParseResult.Success(transform(result.value), result.next)
            is ParseResult.Failure -> result
        }
    }
}

// Helpers de construcción (azúcar sintáctico)
fun seq(vararg rules: Rule) = Seq(rules.toList())
fun choice(vararg rules: Rule) = Choice(rules.toList())
fun many(rule: Rule) = Many(rule)
fun opt(rule: Rule) = Opt(rule)
fun token(type: TokenType, expected: String = type.name) = TokenRule(type, expected)
fun action(rule: Rule, transform: (Any?) -> Any?) = Action(rule, transform)