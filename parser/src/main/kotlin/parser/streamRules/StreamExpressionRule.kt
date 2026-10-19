package parser.streamRules

import ast.BinaryExpression
import ast.Expr
import ast.Identifier
import ast.NumberLiteral
import ast.StringLiteral
import parser.grammar.StreamTerminals.LEFT_PAREN
import parser.grammar.StreamTerminals.RIGHT_PAREN
import parser.stream.Choice
import parser.stream.StreamRule
import parser.stream.action
import parser.stream.many
import parser.stream.ref
import parser.stream.seq
import parser.stream.token
import token.Token
import token.TokenType

class StreamExpressionRule(
    extraPrimaries: List<StreamRule> = emptyList(),
    operatorLevels: List<Set<TokenType>> = DEFAULT_OPERATOR_LEVELS,
) {
    private val primary: StreamRule = Choice(basePrimaries() + extraPrimaries)

    val expression: StreamRule =
        operatorLevels.fold(primary) { level, operators -> withOperatorLevel(level, operators) }

    private fun basePrimaries(): List<StreamRule> =
        listOf(
            action(token(TokenType.NUMBER_LITERAL)) { t -> NumberLiteral((t as Token).value.toDouble(), t.start) },
            action(token(TokenType.STRING_LITERAL)) { t -> StringLiteral((t as Token).value, t.start) },
            action(token(TokenType.IDENTIFIER)) { t -> Identifier((t as Token).value, t.start) },
            action(seq(LEFT_PAREN, ref { expression }, RIGHT_PAREN)) { values ->
                @Suppress("UNCHECKED_CAST")
                (values as List<*>)[1] as Expr
            },
        )

    private fun withOperatorLevel(
        level: StreamRule,
        operators: Set<TokenType>,
    ): StreamRule =
        action(
            seq(level, many(seq(Choice(operators.map { token(it) }), level))),
        ) { values -> foldLeftAssociative(values) }

    @Suppress("UNCHECKED_CAST")
    private fun foldLeftAssociative(values: Any?): Expr {
        val (first, operations) = values as List<*>
        var left = first as Expr

        for (operation in operations as List<*>) {
            val (operatorToken, right) = operation as List<*>
            left = BinaryExpression(left, (operatorToken as Token).value, right as Expr, left.position)
        }

        return left
    }

    companion object {
        val DEFAULT_OPERATOR_LEVELS =
            listOf(
                setOf(TokenType.STAR, TokenType.SLASH),
                setOf(TokenType.PLUS, TokenType.MINUS),
            )
    }
}
