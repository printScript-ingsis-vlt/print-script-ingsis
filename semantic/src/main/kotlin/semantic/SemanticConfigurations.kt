package semantic

import ast.PrintScriptVersion
import ast.VersionConfigurationProvider
import semantic.expressions.ExpressionSemanticHandler
import semantic.handlers.expressions.BinaryExpressionSemanticHandler
import semantic.handlers.expressions.BooleanLiteralSemanticHandler
import semantic.handlers.expressions.IdentifierSemanticHandler
import semantic.handlers.expressions.NumberLiteralSemanticHandler
import semantic.handlers.expressions.StringLiteralSemanticHandler
import semantic.handlers.expressions.read.ReadEnvExpressionSemanticHandler
import semantic.handlers.expressions.read.ReadInputExpressionSemanticHandler
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
object SemanticConfigurations : VersionConfigurationProvider<SemanticConfiguration> {
    private val v1_0SupportedTypes = setOf("number", "string")
    private val v1_1SupportedTypes = setOf("number", "string", "boolean")

    val v1_0: SemanticConfiguration
        get() =
            SemanticConfiguration(
                statementHandlers =
                    listOf(
                        VariableDeclarationSemanticHandler(
                            supportedTypes = v1_0SupportedTypes,
                            constantsAllowed = false,
                        ),
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
                        VariableDeclarationSemanticHandler(
                            supportedTypes = v1_1SupportedTypes,
                            constantsAllowed = true,
                        ),
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
                        ReadInputExpressionSemanticHandler(v1_1SupportedTypes),
                        ReadEnvExpressionSemanticHandler(v1_1SupportedTypes),
                    ),
            )

    override fun getConfiguration(version: PrintScriptVersion): SemanticConfiguration =
        when (version) {
            PrintScriptVersion.V1_0 -> v1_0
            PrintScriptVersion.V1_1 -> v1_1
        }
}
