package semantic.handlers.statements

import ast.Assignment
import ast.Stmt
import result.SemanticError
import semantic.SemanticContext
import semantic.StatementSemanticHandler
import semantic.StatementSemanticTraversal
import semantic.expressions.ExpressionAnalysis

class AssignmentSemanticHandler : StatementSemanticHandler {
    override fun canHandle(statement: Stmt): Boolean = statement is Assignment

    // 1 - Busca la variable destino
    // 2 - Analiza la expresión asignada
    // 3 - Verifica que el destino exista
    // 4 - Verifica que el tipo del valor coincida con el tipo de la variable
    override fun validate(
        statement: Stmt,
        context: SemanticContext,
        analyzeExpression: (ast.Expr) -> ExpressionAnalysis,
        traversal: StatementSemanticTraversal,
    ): List<SemanticError> {
        val assignment = statement as Assignment
        val symbol = context.symbols.lookup(assignment.name)
        val valueAnalysis = analyzeExpression(assignment.value)
        val errors = valueAnalysis.errors.toMutableList()

        if (symbol == null) {
            errors.add(0, SemanticError(assignment.position, "Variable '${assignment.name}' is not declared"))
        } else if (valueAnalysis.type != null && valueAnalysis.type != symbol.type) {
            errors.add(
                SemanticError(
                    assignment.position,
                    "Cannot assign ${valueAnalysis.type} to ${symbol.type}",
                ),
            )
        }

        return errors
    }

    override fun updateEnvironment(
        statement: Stmt,
        context: SemanticContext,
        analyzeExpression: (ast.Expr) -> ExpressionAnalysis,
        traversal: StatementSemanticTraversal,
    ) {
        val assignment = statement as Assignment
        if (context.symbols.lookup(assignment.name) == null) return
        val valueAnalysis = analyzeExpression(assignment.value)
        context.symbols.assign(assignment.name, valueAnalysis.knownNumberValue)
    }
}
