package formatter

import ast.PrintScriptVersion
import ast.VersionConfigurationProvider
import formatter.handlers.expressions.BinaryExpressionHandler
import formatter.handlers.expressions.BooleanLiteralHandler
import formatter.handlers.expressions.IdentifierHandler
import formatter.handlers.expressions.NumberLiteralHandler
import formatter.handlers.expressions.ReadEnvExpressionHandler
import formatter.handlers.expressions.ReadInputExpressionHandler
import formatter.handlers.expressions.StringLiteralHandler
import formatter.handlers.statements.AssignmentHandler
import formatter.handlers.statements.IfStatementHandler
import formatter.handlers.statements.PrintStatementHandler
import formatter.handlers.statements.VariableDeclarationHandler

object FormatterConfigurations : VersionConfigurationProvider<FormatterConfiguration> {
    val v1_0 =
        FormatterConfiguration(
            statementHandlers =
                listOf(
                    VariableDeclarationHandler(),
                    AssignmentHandler(),
                    PrintStatementHandler(),
                ),
            expressionHandlers =
                listOf(
                    NumberLiteralHandler(),
                    StringLiteralHandler(),
                    IdentifierHandler(),
                    BinaryExpressionHandler(),
                ),
        )

    val v1_1 =
        FormatterConfiguration(
            statementHandlers = v1_0.statementHandlers + IfStatementHandler(),
            expressionHandlers =
                v1_0.expressionHandlers +
                    listOf(
                        BooleanLiteralHandler(),
                        ReadInputExpressionHandler(),
                        ReadEnvExpressionHandler(),
                    ),
        )

    val default = v1_1

    override fun getConfiguration(version: PrintScriptVersion): FormatterConfiguration =
        when (version) {
            PrintScriptVersion.V1_0 -> v1_0
            PrintScriptVersion.V1_1 -> v1_1
        }
}
