package parser.grammar

import ast.BooleanLiteral
import parser.engine.Rule
import parser.engine.action
import parser.engine.choice
import parser.engine.token
import token.Token
import token.TokenType

/**
 * true | false -> BooleanLiteral
 *
 * Se pasa como extraPrimary a ExpressionRule, solo en la GrammarConfiguration de 1.1+.
 * En 1.0 el lexer nunca emite TRUE/FALSE, así que ni siquiera hace falta excluirla ahí.
 */
object BooleanLiteralRule {
    val rule: Rule =
        choice(
            action(token(TokenType.TRUE)) { t -> BooleanLiteral(true, (t as Token).start) },
            action(token(TokenType.FALSE)) { t -> BooleanLiteral(false, (t as Token).start) },
        )
}
