package parser.grammar

import parser.engine.Choice
import parser.engine.Rule
import parser.engine.ref

object GrammarConfigurations {
    private val expression = ExpressionRule().expression
    private val expressionV1_1 = ExpressionRule(extraPrimaries = listOf(BooleanLiteralRule.rule)).expression

    val v1_0 =
        GrammarConfiguration(
            statementRules =
                listOf(
                    DeclarationRule(expression),
                    AssignmentRule(expression),
                    PrintStatementRule(expression),
                ),
        )

    val v1_1 = buildV11()

    private fun buildV11(): GrammarConfiguration {
        lateinit var statementRules: List<StatementRule>
        val statementDispatcher: Rule = ref { Choice(statementRules.map { it.rule }) }

        statementRules =
            listOf(
                DeclarationRule(expressionV1_1, extraTypeTokens = listOf(Terminals.BOOLEAN)),
                AssignmentRule(expressionV1_1),
                PrintStatementRule(expressionV1_1),
                IfStatementRule(expressionV1_1, statementDispatcher),
            )

        return GrammarConfiguration(statementRules)
    }

    val default = v1_0
}
