package semantic

import semantic.expressions.ExpressionSemanticHandler
import semantic.handlers.expressions.BinaryExpressionSemanticHandler
import semantic.handlers.expressions.BooleanLiteralSemanticHandler
import semantic.handlers.expressions.IdentifierSemanticHandler
import semantic.handlers.expressions.NumberLiteralSemanticHandler
import semantic.handlers.expressions.StringLiteralSemanticHandler
import semantic.handlers.statements.AssignmentSemanticHandler
import semantic.handlers.statements.IfStatementSemanticHandler
import semantic.handlers.statements.PrintStatementSemanticHandler
import semantic.handlers.statements.VariableDeclarationSemanticHandler

/** Conjunto de handlers que define el comportamiento semántico de una versión. */
data class SemanticConfiguration(
    val statementHandlers: List<StatementSemanticHandler>,
    val expressionHandlers: List<ExpressionSemanticHandler>,
)

/** Configuraciones de handlers semánticos disponibles por versión de PrintScript. */
object SemanticConfigurations {
    val v1_0: SemanticConfiguration
        get() =
            SemanticConfiguration(
                statementHandlers =
                    listOf(
                        VariableDeclarationSemanticHandler(setOf("number", "string")),
                        AssignmentSemanticHandler(),
                        PrintStatementSemanticHandler(),
                    ),
                expressionHandlers =
                    listOf(
                        NumberLiteralSemanticHandler(),
                        StringLiteralSemanticHandler(),
                        IdentifierSemanticHandler(),
                        BinaryExpressionSemanticHandler(),
                    ),
            )

    val v1_1: SemanticConfiguration
        get() =
            SemanticConfiguration(
                statementHandlers =
                    listOf(
                        VariableDeclarationSemanticHandler(setOf("number", "string", "boolean")),
                        AssignmentSemanticHandler(),
                        PrintStatementSemanticHandler(),
                        IfStatementSemanticHandler(),
                    ),
                expressionHandlers =
                    listOf(
                        NumberLiteralSemanticHandler(),
                        StringLiteralSemanticHandler(),
                        BooleanLiteralSemanticHandler(),
                        IdentifierSemanticHandler(),
                        BinaryExpressionSemanticHandler(),
                    ),
            )
}
