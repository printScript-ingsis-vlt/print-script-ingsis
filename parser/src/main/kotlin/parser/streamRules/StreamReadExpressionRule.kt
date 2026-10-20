package parser.streamRules

import ast.Expr
import ast.ReadEnvExpression
import ast.ReadInputExpression
import parser.grammar.StreamTerminals.LEFT_PAREN
import parser.grammar.StreamTerminals.READENV
import parser.grammar.StreamTerminals.READINPUT
import parser.grammar.StreamTerminals.RIGHT_PAREN
import parser.stream.StreamRule
import parser.stream.action
import parser.stream.seq
import token.Token

class StreamReadInputExpressionRule(expression: StreamRule) {
    val rule: StreamRule =
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

class StreamReadEnvExpressionRule(expression: StreamRule) {
    val rule: StreamRule =
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
