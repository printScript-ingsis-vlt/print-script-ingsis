package semantic.handlers.expressions.read

import ast.Expr
import ast.ReadInputExpression
import result.SemanticError
import semantic.SemanticContext
import semantic.expressions.ExpressionAnalysis
import semantic.expressions.ExpressionSemanticHandler

// Analiza una lectura desde el expectedType
class ReadInputExpressionSemanticHandler(
    private val supportedReturnTypes: Set<String>,
) : ExpressionSemanticHandler {
    override fun canHandle(expression: Expr): Boolean = expression is ReadInputExpression

    override fun analyze(
        expression: Expr,
        context: SemanticContext,
        expectedType: String?,
        analyzeChild: (Expr, String?) -> ExpressionAnalysis,
    ): ExpressionAnalysis {
        val readInput = expression as ReadInputExpression
        val promptAnalysis = analyzeChild(readInput.prompt, "string")
        val errors = promptAnalysis.errors.toMutableList()
        val returnType = expectedType?.takeIf { it in supportedReturnTypes }

        if (promptAnalysis.type != null && promptAnalysis.type != "string") {
            errors.add(SemanticError(readInput.prompt.position, "readInput prompt must be a string"))
        }

        when {
            expectedType == null -> {
                errors.add(
                    SemanticError(
                        readInput.position,
                        "readInput must be used where string, number, or boolean is expected",
                    ),
                )
            }
            returnType == null -> {
                errors.add(
                    SemanticError(
                        readInput.position,
                        "readInput cannot produce values of type '$expectedType'",
                    ),
                )
            }
        }

        return ExpressionAnalysis(
            type = returnType,
            errors = errors,
        )
    }
}
