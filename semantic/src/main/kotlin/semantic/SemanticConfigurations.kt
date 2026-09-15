package semantic

import semantic.expressions.ExpressionSemanticAnalyzer
import semantic.handlers.expressions.BinaryExpressionSemanticHandler
import semantic.handlers.expressions.IdentifierSemanticHandler
import semantic.handlers.expressions.NumberLiteralSemanticHandler
import semantic.handlers.expressions.StringLiteralSemanticHandler
import semantic.handlers.statements.AssignmentSemanticHandler
import semantic.handlers.statements.PrintStatementSemanticHandler
import semantic.handlers.statements.VariableDeclarationSemanticHandler

/** Conjunto de handlers que define el comportamiento semántico de una versión. */
data class SemanticConfiguration(
    val statementHandlers: List<StatementSemanticHandler>,
)

/** Configuraciones de handlers semánticos disponibles por versión de PrintScript. */
object SemanticConfigurations {
    val v1_0: SemanticConfiguration
        get() = SemanticConfiguration(statementHandlers())

    val v1_1: SemanticConfiguration
        get() = SemanticConfiguration(statementHandlers())

    private fun statementHandlers(): List<StatementSemanticHandler> {
        val expressionAnalyzer = expressionAnalyzer()

        return listOf(
            VariableDeclarationSemanticHandler(expressionAnalyzer),
            AssignmentSemanticHandler(expressionAnalyzer),
            PrintStatementSemanticHandler(expressionAnalyzer),
        )
    }

    private fun expressionAnalyzer(): ExpressionSemanticAnalyzer =
        ExpressionSemanticAnalyzer(
            listOf(
                NumberLiteralSemanticHandler(),
                StringLiteralSemanticHandler(),
                IdentifierSemanticHandler(),
                BinaryExpressionSemanticHandler(),
            ),
        )
}
