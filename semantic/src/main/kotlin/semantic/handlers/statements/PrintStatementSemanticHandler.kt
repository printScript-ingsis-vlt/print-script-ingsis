package semantic.handlers.statements

import ast.PrintStatement
import ast.Stmt
import result.SemanticError
import semantic.SemanticContext
import semantic.StatementSemanticHandler
import semantic.StatementSemanticTraversal
import semantic.expressions.ExpressionAnalysis

class PrintStatementSemanticHandler : StatementSemanticHandler {
    override fun canHandle(statement: Stmt): Boolean = statement is PrintStatement

    override fun validate(
        statement: Stmt,
        context: SemanticContext,
        analyzeExpression: (ast.Expr, String?) -> ExpressionAnalysis,
        traversal: StatementSemanticTraversal,
    ): List<SemanticError> {
        val printStatement = statement as PrintStatement

        return analyzeExpression(printStatement.argument, "string").errors
    }

    // No actualiza el entorno
    override fun updateEnvironment(
        statement: Stmt,
        context: SemanticContext,
        analyzeExpression: (ast.Expr, String?) -> ExpressionAnalysis,
        traversal: StatementSemanticTraversal,
    ) = Unit
}
