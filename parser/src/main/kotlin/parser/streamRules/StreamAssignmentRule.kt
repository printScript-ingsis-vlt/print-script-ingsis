package parser.streamRules

import ast.Assignment
import ast.Expr
import parser.grammar.StreamTerminals.EQUAL
import parser.grammar.StreamTerminals.IDENTIFIER
import parser.grammar.StreamTerminals.SEMICOLON
import parser.stream.StreamRule
import parser.stream.action
import parser.stream.seq
import token.Token

class StreamAssignmentRule(expression: StreamRule) {
    val rule: StreamRule =
        action(seq(IDENTIFIER, EQUAL, expression, SEMICOLON)) { values ->
            val list = values as List<Any>
            val nameToken = list[0] as Token
            val value = list[2] as Expr
            Assignment(name = nameToken.value, value = value, position = nameToken.start)
        }
}
