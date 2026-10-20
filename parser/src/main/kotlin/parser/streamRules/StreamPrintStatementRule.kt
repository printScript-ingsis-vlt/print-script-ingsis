package parser.streamRules

import ast.Expr
import ast.PrintStatement
import parser.grammar.StreamTerminals.LEFT_PAREN
import parser.grammar.StreamTerminals.RIGHT_PAREN
import parser.grammar.StreamTerminals.SEMICOLON
import parser.stream.StreamRule
import parser.stream.action
import parser.stream.literal
import parser.stream.seq
import token.Token
import token.TokenType

class StreamPrintStatementRule(expression: StreamRule) {
    val rule: StreamRule =
        action(
            seq(PRINTLN, LEFT_PAREN, expression, RIGHT_PAREN, SEMICOLON),
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val list = values as List<Any?>

            val printlnToken = list[0] as Token
            val argument = list[ARGUMENT_INDEX] as Expr

            PrintStatement(argument = argument, position = printlnToken.start)
        }

    private companion object {
        val PRINTLN = literal(TokenType.IDENTIFIER, "println")
        const val ARGUMENT_INDEX = 2
    }
}
