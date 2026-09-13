package parser.grammar

import ast.Expr
import ast.VariableDeclaration
import parser.engine.Rule
import parser.engine.action
import parser.engine.opt
import parser.engine.seq
import parser.grammar.Terminals.COLON
import parser.grammar.Terminals.EQUAL
import parser.grammar.Terminals.IDENTIFIER
import parser.grammar.Terminals.LET
import parser.grammar.Terminals.SEMICOLON
import token.Token

/** let IDENTIFIER : IDENTIFIER ('=' expression)? ';' */
class DeclarationRule(expression: Rule) : StatementRule {
    override val rule: Rule =
        action(
            seq(
                LET,
                IDENTIFIER,
                COLON,
                IDENTIFIER,
                opt(seq(EQUAL, expression)),
                SEMICOLON,
            ),
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val list = values as List<Any?>

            val letToken = list[0] as Token
            val nameToken = list[1] as Token
            val typeToken = list[TYPE_TOKEN_INDEX] as Token

            val value: Expr? =
                when (val optionalValue = list[OPTIONAL_VALUE_INDEX]) {
                    null -> null
                    is List<*> -> optionalValue[1] as Expr
                    else -> null
                }

            VariableDeclaration(
                name = nameToken.value,
                type = typeToken.value,
                value = value,
                position = letToken.start,
            )
        }

    private companion object {
        const val TYPE_TOKEN_INDEX = 3
        const val OPTIONAL_VALUE_INDEX = 4
    }
}
