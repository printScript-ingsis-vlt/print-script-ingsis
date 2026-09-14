package parser.grammar

object GrammarConfigurations {
    private val expression = ExpressionRule().expression

    val v1_0 =
        GrammarConfiguration(
            statementRules =
                listOf(
                    DeclarationRule(expression),
                    AssignmentRule(expression),
                    PrintStatementRule(expression),
                ),
        )

    val default = v1_0
}
