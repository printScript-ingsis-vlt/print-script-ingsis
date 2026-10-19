package parser.streamRules

import ast.Expr
import ast.VariableDeclaration
import parser.grammar.StreamTerminals.EQUAL
import parser.grammar.StreamTerminals.LET
import parser.grammar.StreamTerminals.SEMICOLON
import parser.stream.StreamRule
import parser.stream.action
import parser.stream.opt
import parser.stream.seq
import token.Token

class StreamDeclarationRule(
    expression: StreamRule,
    extraTypeTokens: List<StreamRule> = emptyList(),
) {
    private val typedName: StreamRule = StreamTypedNameRule(extraTypeTokens).rule

    private val initializer: StreamRule =
        action(seq(EQUAL, expression)) { parts ->
            val list = parts as List<*>
            list[1] as Expr
        }

    val rule: StreamRule =
        action(seq(LET, typedName, opt(initializer), SEMICOLON)) { values ->
            val list = values as List<*>
            val letToken = list[0] as Token
            val declared = list[1] as TypedName
            val value = list[2] as Expr?

            VariableDeclaration(
                name = declared.name.value,
                type = declared.type.value,
                value = value,
                position = letToken.start,
            )
        }
}
