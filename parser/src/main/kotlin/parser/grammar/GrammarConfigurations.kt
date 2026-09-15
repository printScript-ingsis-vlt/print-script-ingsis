package parser.grammar

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

    val v1_1 =
        GrammarConfiguration(
            statementRules =
                listOf(
                    DeclarationRule(expressionV1_1, extraTypeTokens = listOf(Terminals.BOOLEAN)),
                    AssignmentRule(expressionV1_1),
                    PrintStatementRule(expressionV1_1),
                ),
        )

    val default = v1_0
}
