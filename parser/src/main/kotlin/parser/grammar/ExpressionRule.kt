package parser.grammar

import ast.BinaryExpression
import ast.Expr
import ast.Identifier
import ast.NumberLiteral
import ast.StringLiteral
import parser.engine.Choice
import parser.engine.Rule
import parser.engine.action
import parser.engine.many
import parser.engine.ref
import parser.engine.seq
import parser.engine.token
import parser.grammar.Terminals.LEFT_PAREN
import parser.grammar.Terminals.RIGHT_PAREN
import token.Token
import token.TokenType

/**
 * expresiones con precedencia, extensible sin editar esta clase:
 * - [extraPrimaries] suma alternativas a `primary` (ej. literales booleanos)
 * - [operatorLevels] suma niveles de precedencia (ej. comparaciones/lógicos), de mayor a
 *   menor precedencia
 *
 * primary := NUMBER_LITERAL | STRING_LITERAL | IDENTIFIER | '(' expression ')' | extraPrimaries
 * cada nivel de operatorLevels es: nivelAnterior (operadorDelNivel nivelAnterior)*
 */
class ExpressionRule(
    extraPrimaries: List<Rule> = emptyList(),
    operatorLevels: List<Set<TokenType>> = DEFAULT_OPERATOR_LEVELS,
) {
    private val primary: Rule = Choice(basePrimaries() + extraPrimaries)

    val expression: Rule =
        operatorLevels.fold(primary) { level, operators -> withOperatorLevel(level, operators) }

    private fun basePrimaries(): List<Rule> =
        listOf(
            action(token(TokenType.NUMBER_LITERAL)) { t -> NumberLiteral((t as Token).value.toDouble(), t.start) },
            action(token(TokenType.STRING_LITERAL)) { t -> StringLiteral((t as Token).value, t.start) },
            action(token(TokenType.IDENTIFIER)) { t -> Identifier((t as Token).value, t.start) },
            action(seq(LEFT_PAREN, ref { expression }, RIGHT_PAREN)) { values ->
                (values as List<*>)[1] as Expr
            },
        )

    private fun withOperatorLevel(
        level: Rule,
        operators: Set<TokenType>,
    ): Rule =
        action(
            seq(level, many(seq(Choice(operators.map { token(it) }), level))),
        ) { values -> foldLeftAssociative(values) }

    // pliega [primero, [[operador, siguiente], [operador, siguiente], ...]] en BinaryExpression
    // anidados de izquierda a derecha
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

    private companion object {
        // orden: de mayor a menor precedencia -> '*','/' se evalúan antes que '+','-'
        val DEFAULT_OPERATOR_LEVELS =
            listOf(
                setOf(TokenType.STAR, TokenType.SLASH),
                setOf(TokenType.PLUS, TokenType.MINUS),
            )
    }
}
