package parser.grammar

/** qué reglas de statement de primer nivel acepta el parser */
data class GrammarConfiguration(
    val statementRules: List<StatementRule>,
)
