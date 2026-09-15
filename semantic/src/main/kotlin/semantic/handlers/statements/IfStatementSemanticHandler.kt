package semantic.handlers.statements

import ast.Expr
import ast.Identifier
import ast.IfStatement
import ast.Stmt
import result.SemanticError
import semantic.SemanticContext
import semantic.StatementSemanticHandler
import semantic.StatementSemanticTraversal
import semantic.expressions.ExpressionAnalysis

// valida bloques condicionales y los mergea
class IfStatementSemanticHandler : StatementSemanticHandler {
    override fun canHandle(statement: Stmt): Boolean = statement is IfStatement

    override fun validate(
        statement: Stmt,
        context: SemanticContext,
        analyzeExpression: (Expr, String?) -> ExpressionAnalysis,
        traversal: StatementSemanticTraversal,
    ): List<SemanticError> {
        val ifStatement = statement as IfStatement
        val errors = validateCondition(ifStatement, analyzeExpression).toMutableList()

        // valida la thenBranch y si existe la elseBranch
        errors.addAll(validateBranch(ifStatement.thenBranch, context, traversal))
        ifStatement.elseBranch?.let { elseBranch ->
            errors.addAll(validateBranch(elseBranch, context, traversal))
        }

        return errors
    }

    // updatea cada rama por individual y las mergea en el context padre
    override fun updateEnvironment(
        statement: Stmt,
        context: SemanticContext,
        analyzeExpression: (Expr, String?) -> ExpressionAnalysis,
        traversal: StatementSemanticTraversal,
    ) {
        val ifStatement = statement as IfStatement
        val thenContext = SemanticContext(context.symbols.copy())
        val elseContext = SemanticContext(context.symbols.copy())

        thenContext.symbols.withScope {
            traversal.update(ifStatement.thenBranch, thenContext)
        }
        ifStatement.elseBranch?.let { elseBranch ->
            elseContext.symbols.withScope {
                traversal.update(elseBranch, elseContext)
            }
        }

        context.symbols.mergeConditionalBranches(thenContext.symbols, elseContext.symbols)
    }

    // exige que la condicion sea un boolean
    private fun validateCondition(
        statement: IfStatement,
        analyzeExpression: (Expr, String?) -> ExpressionAnalysis,
    ): List<SemanticError> {
        val condition = statement.condition
        if (condition !is Identifier) {
            return listOf(SemanticError(condition.position, "If condition must be a boolean variable"))
        }

        // existe un tipo pero no es boolean
        val analysis = analyzeExpression(condition, "boolean")
        val errors = analysis.errors.toMutableList()
        if (analysis.type != null && analysis.type != "boolean") {
            errors.add(SemanticError(condition.position, "If condition variable '${condition.name}' must be boolean"))
        }

        return errors
    }

    private fun validateBranch(
        branch: List<Stmt>,
        context: SemanticContext,
        traversal: StatementSemanticTraversal,
    ): List<SemanticError> {
        val branchContext = SemanticContext(context.symbols.copy())

        return branchContext.symbols.withScope {
            traversal.validateAndUpdate(branch, branchContext)
        }
    }
}
