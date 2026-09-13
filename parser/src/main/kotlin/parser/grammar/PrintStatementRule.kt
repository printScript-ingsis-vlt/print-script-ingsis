package parser.grammar

import ast.Expr
import ast.PrintStatement
import parser.engine.Rule
import parser.engine.action
import parser.engine.literal
import parser.engine.seq
import parser.grammar.Terminals.LEFT_PAREN
import parser.grammar.Terminals.RIGHT_PAREN
import parser.grammar.Terminals.SEMICOLON
import token.Token
import token.TokenType

/**
 * 'println' '(' expression ')' ';'
 * "println" no es keyword del lexer (no aparece en LexerConfigurations), así que llega
 * como un IDENTIFIER común y se matchea por su valor exacto.
 */
class PrintStatementRule(expression: Rule) : StatementRule {
    override val rule: Rule =
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
