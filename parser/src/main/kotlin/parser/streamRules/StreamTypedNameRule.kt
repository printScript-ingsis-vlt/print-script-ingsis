package parser.streamRules

import parser.grammar.StreamTerminals.COLON
import parser.grammar.StreamTerminals.IDENTIFIER
import parser.stream.Choice
import parser.stream.StreamRule
import parser.stream.action
import parser.stream.seq
import token.Token

class StreamTypedNameRule(extraTypeTokens: List<StreamRule> = emptyList()) {
    private val type: StreamRule = Choice(listOf(IDENTIFIER) + extraTypeTokens)

    val rule: StreamRule =
        action(seq(IDENTIFIER, COLON, type)) { parts ->
            val list = parts as List<*>
            val name = list[0] as Token
            val declaredType = list[2] as Token
            TypedName(name, declaredType)
        }
}
