package formatter

data class FormatterConfiguration(
    val statementHandlers: List<StatementHandler>,
    val expressionHandlers: List<ExpressionHandler>,
) {
    init {
        require(statementHandlers.isNotEmpty()) { "Debe existir al menos un StatementHandler" }
        require(expressionHandlers.isNotEmpty()) { "Debe existir al menos un ExpressionHandler" }
    }
}
