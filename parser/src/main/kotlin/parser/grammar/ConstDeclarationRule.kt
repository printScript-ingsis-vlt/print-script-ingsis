package parser.grammar

import ast.Expr
import ast.VariableDeclaration
import parser.engine.Rule
import parser.engine.action
import parser.engine.seq
import parser.grammar.Terminals.CONST
import parser.grammar.Terminals.EQUAL
import parser.grammar.Terminals.SEMICOLON
import token.Token

/**
 * const IDENTIFIER : tipo '=' expression ';'
 *
 * A diferencia de let ([DeclarationRule]), el inicializador es obligatorio y el
 * resultado es un VariableDeclaration con mutable = false.
 */
class ConstDeclarationRule(
    expression: Rule,
    extraTypeTokens: List<Rule> = emptyList(),
) : StatementRule {
    private val typedName: Rule = TypedNameRule(extraTypeTokens).rule
    private val initializer: Rule =
        action(seq(EQUAL, expression)) { parts -> (parts as List<*>)[1] as Expr }

    override val rule: Rule =
        action(seq(CONST, typedName, initializer, SEMICOLON)) { values ->
            val (constToken, declared, value) = values as List<*>
            declared as TypedName

            VariableDeclaration(
                name = declared.name.value,
                type = declared.type.value,
                value = value as Expr,
                position = (constToken as Token).start,
                mutable = false,
            )
        }
}
