package parser.grammar

/** qué reglas de statement de primer nivel acepta el parser */
data class GrammarConfiguration(
    val statementRules: List<StatementRule>,
) {
    init {
        require(statementRules.isNotEmpty()) {
            "GrammarConfiguration necesita al menos una StatementRule, si no ninguna sentencia podría parsearse"
        }
    }
}
