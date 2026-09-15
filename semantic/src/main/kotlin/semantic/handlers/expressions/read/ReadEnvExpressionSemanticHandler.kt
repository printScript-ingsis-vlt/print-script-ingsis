package semantic.handlers.expressions.read

import ast.Expr
import ast.ReadEnvExpression
import result.SemanticError
import semantic.SemanticContext
import semantic.expressions.ExpressionAnalysis
import semantic.expressions.ExpressionSemanticHandler

// Analiza una lectura de variable de entorno sin acceder al environment del proceso
class ReadEnvExpressionSemanticHandler(
    private val supportedReturnTypes: Set<String>,
) : ExpressionSemanticHandler {
    override fun canHandle(expression: Expr): Boolean = expression is ReadEnvExpression

    override fun analyze(
        expression: Expr,
        context: SemanticContext,
        expectedType: String?,
        analyzeChild: (Expr, String?) -> ExpressionAnalysis,
    ): ExpressionAnalysis {
        val readEnv = expression as ReadEnvExpression
        val variableNameAnalysis = analyzeChild(readEnv.envVariableName, "string")
        val errors = variableNameAnalysis.errors.toMutableList()
        val returnType = expectedType?.takeIf { it in supportedReturnTypes }

        // solo se acepta variables con tipo conocido string
        if (variableNameAnalysis.type != null && variableNameAnalysis.type != "string") {
            errors.add(
                SemanticError(
                    readEnv.envVariableName.position,
                    "readEnv environment variable name must be a string",
                ),
            )
        }

        when {
            expectedType == null -> {
                errors.add(
                    SemanticError(
                        readEnv.position,
                        "readEnv must be used where string, number, or boolean is expected",
                    ),
                )
            }
            returnType == null -> {
                errors.add(
                    SemanticError(
                        readEnv.position,
                        "readEnv cannot produce values of type '$expectedType'",
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
