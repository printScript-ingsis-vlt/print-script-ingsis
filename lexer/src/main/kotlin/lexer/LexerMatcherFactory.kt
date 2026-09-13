package lexer

import lexer.implementations.IdentifierMatcher
import lexer.implementations.NumberMatcher
import lexer.implementations.OperatorMatcher
import lexer.implementations.StringMatcher

object LexerMatcherFactory {
    fun create(configuration: LexerConfiguration): List<TokenMatcher> =
        listOf(
            StringMatcher(),
            NumberMatcher(),
            IdentifierMatcher(configuration.keywords),
            OperatorMatcher(configuration.operators),
        )
}
