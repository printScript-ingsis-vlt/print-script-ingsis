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
    private data class TypedName(val name: Token, val type: Token)

    // IDENTIFIER ':' IDENTIFIER -> nombre y tipo ya extraídos, sin listas anidadas sueltas
    private val typedName: Rule =
        action(seq(IDENTIFIER, COLON, IDENTIFIER)) { parts ->
            val (name, _, type) = parts as List<*>
            TypedName(name as Token, type as Token)
        }

    // '=' expression -> solo el valor, el '=' no aporta nada al AST
    private val initializer: Rule =
        action(seq(EQUAL, expression)) { parts -> (parts as List<*>)[1] as Expr }

    override val rule: Rule =
        action(seq(LET, typedName, opt(initializer), SEMICOLON)) { values ->
            val (letToken, declared, value) = values as List<*>
            declared as TypedName

            VariableDeclaration(
                name = declared.name.value,
                type = declared.type.value,
                value = value as Expr?,
                position = (letToken as Token).start,
            )
        }
}
