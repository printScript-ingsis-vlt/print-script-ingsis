package interpreter

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

object InterpreterConfigurations {
    val v1_0 =
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

    val default = v1_0
}
