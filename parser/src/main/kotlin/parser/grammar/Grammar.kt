package parser.grammar

import ast.Assignment
import ast.BinaryExpression
import ast.Expr
import ast.Identifier
import ast.NumberLiteral
import ast.StringLiteral
import ast.VariableDeclaration
import parser.engine.Rule
import parser.engine.action
import parser.engine.choice
import parser.engine.many
import parser.engine.opt
import parser.engine.ref
import parser.engine.seq
import parser.engine.token
import token.Token
import token.TokenType

object Grammar {
    private const val TYPE_TOKEN_INDEX = 3
    private const val OPTIONAL_VALUE_INDEX = 4

    // ---------- Terminals reutilizables ----------
    val LET = token(TokenType.LET, "let")
    val COLON = token(TokenType.COLON, ":")
    val EQUAL = token(TokenType.EQUAL, "=")
    val SEMICOLON = token(TokenType.SEMICOLON, ";")
    val IDENTIFIER = token(TokenType.IDENTIFIER, "identifier")
    val LEFT_PAREN = token(TokenType.LEFT_PAREN, "(")
    val RIGHT_PAREN = token(TokenType.RIGHT_PAREN, ")")

    // ---------- expression, con precedencia ----------
    // primary := NUMBER_LITERAL | STRING_LITERAL | IDENTIFIER | '(' expression ')'
    // term := primary (('*' | '/') primary)*
    // expression := term (('+' | '-') term)*
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

    // ---------- declaration completa ----------
    val declaration: Rule =
        action(
            seq(
                // 0
                LET,
                // 1 → name
                IDENTIFIER,
                // 2
                COLON,
                // 3 → type
                IDENTIFIER,
                // 4 → value (puede ser null)
                opt(
                    seq(EQUAL, expression),
                ),
                // 5
                SEMICOLON,
            ),
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val list = values as List<Any?>

            val letToken = list[0] as Token
            val nameToken = list[1] as Token
            val typeToken = list[TYPE_TOKEN_INDEX] as Token

            val value: Expr? =
                when (val opt = list[OPTIONAL_VALUE_INDEX]) {
                    null -> null
                    // el segundo elemento de [EQUAL, expression]
                    is List<*> -> opt[1] as Expr
                    else -> null
                }

            VariableDeclaration(
                name = nameToken.value,
                type = typeToken.value,
                value = value,
                position = letToken.start,
            )
        }

    val assignment: Rule =
        action(
            seq(
                // 0 → name
                IDENTIFIER,
                // 1
                EQUAL,
                // 2 → value
                expression,
                // 3
                SEMICOLON,
            ),
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val list = values as List<Any>

            val nameToken = list[0] as Token
            val value = list[2] as Expr

            Assignment(
                name = nameToken.value,
                value = value,
                position = nameToken.start,
            )
        }

    val statement: Rule =
        choice(
            declaration,
            assignment,
        )
}
