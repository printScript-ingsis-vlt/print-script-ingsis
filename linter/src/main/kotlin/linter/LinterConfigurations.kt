package linter

import ast.PrintScriptVersion
import ast.VersionConfigurationProvider
import linter.handlers.expressions.BinaryExpressionHandler
import linter.handlers.expressions.BooleanLiteralHandler
import linter.handlers.expressions.IdentifierHandler
import linter.handlers.expressions.NumberLiteralHandler
import linter.handlers.expressions.ReadEnvExpressionHandler
import linter.handlers.expressions.ReadInputExpressionHandler
import linter.handlers.expressions.StringLiteralHandler
import linter.handlers.statements.AssignmentHandler
import linter.handlers.statements.IfStatementHandler
import linter.handlers.statements.PrintStatementHandler
import linter.handlers.statements.VariableDeclarationHandler

object LinterConfigurations : VersionConfigurationProvider<LinterConfiguration> {
    val v1_0 =
        LinterConfiguration(
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
        LinterConfiguration(
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

    override fun getConfiguration(version: PrintScriptVersion): LinterConfiguration =
        when (version) {
            PrintScriptVersion.V1_0 -> v1_0
            PrintScriptVersion.V1_1 -> v1_1
        }
}
