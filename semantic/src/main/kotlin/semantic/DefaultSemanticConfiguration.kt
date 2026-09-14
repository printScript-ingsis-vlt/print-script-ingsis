package semantic

import semantic.expressions.ExpressionSemanticAnalyzer
import semantic.handlers.expressions.BinaryExpressionSemanticHandler
import semantic.handlers.expressions.IdentifierSemanticHandler
import semantic.handlers.expressions.NumberLiteralSemanticHandler
import semantic.handlers.expressions.StringLiteralSemanticHandler
import semantic.handlers.statements.AssignmentSemanticHandler
import semantic.handlers.statements.PrintStatementSemanticHandler
import semantic.handlers.statements.VariableDeclarationSemanticHandler

/** Construye la composición de handlers que soporta la versión actual del lenguaje.
 *  Cuando se implemente la 1.1 se hara otra version */
object DefaultSemanticConfiguration {
    fun statementHandlers(): List<StatementSemanticHandler> {
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
