package parser.grammar

import ast.BinaryExpression
import ast.Expr
import ast.Identifier
import ast.NumberLiteral
import ast.StringLiteral
import parser.engine.Rule
import parser.engine.action
import parser.engine.choice
import parser.engine.many
import parser.engine.ref
import parser.engine.seq
import parser.engine.token
import parser.grammar.Terminals.LEFT_PAREN
import parser.grammar.Terminals.RIGHT_PAREN
import token.Token
import token.TokenType

/**
 * expresiones con precedencia:
 * primary := NUMBER_LITERAL | STRING_LITERAL | IDENTIFIER | '(' expression ')'
 * term := primary (('*' | '/') primary)*
 * expression := term (('+' | '-') term)*
 */
object ExpressionRule {
    private val primary: Rule =
        choice(
            action(token(TokenType.NUMBER_LITERAL)) { t -> NumberLiteral((t as Token).value.toDouble(), t.start) },
            action(token(TokenType.STRING_LITERAL)) { t -> StringLiteral((t as Token).value, t.start) },
            action(token(TokenType.IDENTIFIER)) { t -> Identifier((t as Token).value, t.start) },
            action(seq(LEFT_PAREN, ref { expression }, RIGHT_PAREN)) { values ->
                (values as List<*>)[1] as Expr
            },
        )

    private val term: Rule =
        action(
            seq(primary, many(seq(choice(token(TokenType.STAR), token(TokenType.SLASH)), primary))),
        ) { values -> foldLeftAssociative(values) }

    val expression: Rule =
        action(
            seq(term, many(seq(choice(token(TokenType.PLUS), token(TokenType.MINUS)), term))),
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
}
