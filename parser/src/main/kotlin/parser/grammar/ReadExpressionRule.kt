package parser.grammar

import ast.Expr
import ast.ReadEnvExpression
import ast.ReadInputExpression
import parser.engine.Rule
import parser.engine.action
import parser.engine.seq
import parser.grammar.Terminals.LEFT_PAREN
import parser.grammar.Terminals.READENV
import parser.grammar.Terminals.READINPUT
import parser.grammar.Terminals.RIGHT_PAREN
import token.Token

/**
 * readInput '(' expression ')' -> ReadInputExpression
 * readEnv '(' expression ')' -> ReadEnvExpression
 *
 * El argumento acepta cualquier expression, igual que println. La consigna restringe
 * "readInput solo puede llamarse con un identificador o un literal, pero esto puede estar
 * prendido o apagado" -es una regla del linter, configurable, no del parser- asi que el
 * parser no debe rechazar nada aca; si lo hiciera, esa regla no se podria apagar nunca.
 *
 * Se pasan como extraPrimary de ExpressionRule, solo en la GrammarConfiguration de 1.1+.
 */
class ReadInputExpressionRule(expression: Rule) {
    val rule: Rule =
        action(seq(READINPUT, LEFT_PAREN, expression, RIGHT_PAREN)) { values ->
            @Suppress("UNCHECKED_CAST")
            val parts = values as List<Any?>

            ReadInputExpression(
                prompt = parts[ARGUMENT_INDEX] as Expr,
                position = (parts[KEYWORD_INDEX] as Token).start,
            )
        }

    private companion object {
        const val KEYWORD_INDEX = 0
        const val ARGUMENT_INDEX = 2
    }
}

class ReadEnvExpressionRule(expression: Rule) {
    val rule: Rule =
        action(seq(READENV, LEFT_PAREN, expression, RIGHT_PAREN)) { values ->
            @Suppress("UNCHECKED_CAST")
            val parts = values as List<Any?>

            ReadEnvExpression(
                envVariableName = parts[ARGUMENT_INDEX] as Expr,
                position = (parts[KEYWORD_INDEX] as Token).start,
            )
        }

    private companion object {
        const val KEYWORD_INDEX = 0
        const val ARGUMENT_INDEX = 2
    }
}
