package semantic.handlers.statements

import ast.PrintStatement
import ast.Stmt
import result.SemanticError
import semantic.SemanticContext
import semantic.StatementSemanticHandler
import semantic.expressions.ExpressionSemanticAnalyzer

class PrintStatementSemanticHandler(
    private val expressionAnalyzer: ExpressionSemanticAnalyzer,
) : StatementSemanticHandler {
    override fun canHandle(statement: Stmt): Boolean = statement is PrintStatement

    override fun validate(
        statement: Stmt,
        context: SemanticContext,
    ): List<SemanticError> {
        val printStatement = statement as PrintStatement

        return expressionAnalyzer.analyze(printStatement.argument, context).errors
    }

    // No actualiza el entorno
    override fun updateEnvironment(
        statement: Stmt,
        context: SemanticContext,
    ) = Unit
}
