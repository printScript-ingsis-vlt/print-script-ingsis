package interpreter

import ast.PrintScriptVersion
import ast.VersionConfigurationProvider
import interpreter.handlers.expressions.BinaryExpressionHandler
import interpreter.handlers.expressions.BooleanExpressionHandler
import interpreter.handlers.expressions.IdentifierHandler
import interpreter.handlers.expressions.NumberLiteralHandler
import interpreter.handlers.expressions.ReadEnvExpressionHandler
import interpreter.handlers.expressions.ReadInputExpressionHandler
import interpreter.handlers.expressions.StringLiteralHandler
import interpreter.handlers.statements.AssignmentHandler
import interpreter.handlers.statements.IfElseStatementHandler
import interpreter.handlers.statements.PrintStatementHandler
import interpreter.handlers.statements.VariableDeclarationHandler

object InterpreterConfigurations : VersionConfigurationProvider<InterpreterConfiguration> {
    // Versión 1.0: Únicamente las sentencias y expresiones base iniciales
    val v1_0 =
        InterpreterConfiguration(
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

    // Versión 1.1: Incluye condicionales (IfElse), expresiones booleanas y lecturas I/O (ReadInput/ReadEnv)
    val v1_1 =
        InterpreterConfiguration(
            statementHandlers =
                listOf(
                    VariableDeclarationHandler(),
                    AssignmentHandler(),
                    PrintStatementHandler(),
                    IfElseStatementHandler(),
                ),
            expressionHandlers =
                listOf(
                    NumberLiteralHandler(),
                    StringLiteralHandler(),
                    IdentifierHandler(),
                    BinaryExpressionHandler(),
                    BooleanExpressionHandler(),
                    ReadInputExpressionHandler(),
                    ReadEnvExpressionHandler(),
                ),
        )

    val default = v1_1

    override fun getConfiguration(version: PrintScriptVersion): InterpreterConfiguration =
        when (version) {
            PrintScriptVersion.V1_0 -> v1_0
            PrintScriptVersion.V1_1 -> v1_1
        }
}
