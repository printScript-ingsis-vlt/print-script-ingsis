package parser.grammar

import parser.engine.Choice
import parser.engine.Rule
import parser.engine.action
import parser.engine.seq
import parser.grammar.Terminals.COLON
import parser.grammar.Terminals.IDENTIFIER
import token.Token

/** nombre y tipo ya extraidos de "IDENTIFIER ':' tipo", compartido por let y const */
data class TypedName(val name: Token, val type: Token)

/**
 * IDENTIFIER ':' tipo -> TypedName
 *
 * El tipo es IDENTIFIER por defecto (number, string como texto libre), mas los tokens
 * de [extraTypeTokens] (ej. la keyword `boolean` en 1.1+, que el lexer tokeniza distinto).
 * La usan tanto DeclarationRule (let) como ConstDeclarationRule (const).
 */
class TypedNameRule(extraTypeTokens: List<Rule> = emptyList()) {
    private val type: Rule = Choice(listOf(IDENTIFIER) + extraTypeTokens)

    val rule: Rule =
        action(seq(IDENTIFIER, COLON, type)) { parts ->
            val (name, _, declaredType) = parts as List<*>
            TypedName(name as Token, declaredType as Token)
        }
}
