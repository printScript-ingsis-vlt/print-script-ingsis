package semantic.handlers.statements

import ast.Assignment
import ast.Stmt
import result.SemanticError
import runtime.valuedataclass.NumberValue
import runtime.valuedataclass.StringValue
import semantic.SemanticContext
import semantic.StatementSemanticHandler
import semantic.expressions.ExpressionSemanticAnalyzer

class AssignmentSemanticHandler(
    private val expressionAnalyzer: ExpressionSemanticAnalyzer,
) : StatementSemanticHandler {
    override fun canHandle(statement: Stmt): Boolean = statement is Assignment

    //1 - Busca la variable destino
    //2 - Analiza la expresión asignada
    //3 - Verifica que el destino exista
    //4 - Verifica que el tipo del valor coincida con el tipo de la variable
    override fun validate(
        statement: Stmt,
        context: SemanticContext,
    ): List<SemanticError> {
        val assignment = statement as Assignment
        val variable = context.environment.lookup(assignment.name)
        val valueAnalysis = expressionAnalyzer.analyze(assignment.value, context)
        val errors = valueAnalysis.errors.toMutableList()

        if (variable == null) {
            errors.add(0, SemanticError(assignment.position, "Variable '${assignment.name}' is not declared"))
        } else if (valueAnalysis.type != null && valueAnalysis.type != variable.type) {
            errors.add(
                SemanticError(
                    assignment.position,
                    "Cannot assign ${valueAnalysis.type} to ${variable.type}",
                ),
            )
        }

        return errors
    }

    override fun updateEnvironment(
        statement: Stmt,
        context: SemanticContext,
    ) {
        val assignment = statement as Assignment
        val variable = context.environment.lookup(assignment.name) ?: return
        val assignedValue = if (variable.type == "number") NumberValue(0.0) else StringValue("")

        context.environment.assign(assignment.name, assignedValue)
    }
}
