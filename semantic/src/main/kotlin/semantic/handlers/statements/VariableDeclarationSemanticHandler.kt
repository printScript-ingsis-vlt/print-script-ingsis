package semantic.handlers.statements

import ast.Stmt
import ast.VariableDeclaration
import result.SemanticError
import semantic.SemanticContext
import semantic.StatementSemanticHandler
import semantic.expressions.ExpressionSemanticAnalyzer
import semantic.symbols.SemanticSymbol

class VariableDeclarationSemanticHandler(
    private val expressionAnalyzer: ExpressionSemanticAnalyzer,
) : StatementSemanticHandler {
    override fun canHandle(statement: Stmt): Boolean = statement is VariableDeclaration

    //1 - Analiza la expresión inicializadora, si existe
    //2 - Verifica que el tipo declarado sea number o string
    //3 - Verifica que el tipo inferido del inicializador coincida con el declarado
    override fun validate(
        statement: Stmt,
        context: SemanticContext,
    ): List<SemanticError> {
        val declaration = statement as VariableDeclaration
        val valueAnalysis = declaration.value?.let { expressionAnalyzer.analyze(it, context) }
        val errors = valueAnalysis?.errors.orEmpty().toMutableList()

        if (declaration.type !in SUPPORTED_TYPES) {
            errors.add(SemanticError(declaration.position, "Invalid type '${declaration.type}'"))
        } else if (valueAnalysis?.type != null && valueAnalysis.type != declaration.type) {
            errors.add(
                SemanticError(
                    declaration.position,
                    "Cannot assign ${valueAnalysis.type} to ${declaration.type}",
                ),
            )
        }

        return errors
    }

    // Declara la variable si no hubo errores
    override fun updateEnvironment(
        statement: Stmt,
        context: SemanticContext,
    ) {
        val declaration = statement as VariableDeclaration
        val valueAnalysis = declaration.value?.let { expressionAnalyzer.analyze(it, context) }
        context.symbols.declare(
            declaration.name,
            SemanticSymbol(
                type = declaration.type,
                initialized = declaration.value != null,
                knownNumberValue = valueAnalysis?.knownNumberValue,
            ),
        )
    }

    private companion object {
        val SUPPORTED_TYPES = setOf("number", "string")
    }
}
