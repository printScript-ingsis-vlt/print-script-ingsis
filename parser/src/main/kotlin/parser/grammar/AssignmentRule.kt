package parser.grammar

import ast.Assignment
import ast.Expr
import parser.engine.Rule
import parser.engine.action
import parser.engine.seq
import parser.grammar.Terminals.EQUAL
import parser.grammar.Terminals.IDENTIFIER
import parser.grammar.Terminals.SEMICOLON
import token.Token

/** IDENTIFIER '=' expression ';' */
class AssignmentRule(expression: Rule) : StatementRule {
    override val rule: Rule =
        action(
            seq(IDENTIFIER, EQUAL, expression, SEMICOLON),
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
}
