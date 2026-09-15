package semantic.handlers.expressions

import ast.BinaryExpression
import ast.Expr
import result.SemanticError
import runtime.OperationType
import semantic.SemanticContext
import semantic.expressions.ExpressionAnalysis
import semantic.expressions.ExpressionSemanticHandler

// Analiza expresiones recursivas
class BinaryExpressionSemanticHandler : ExpressionSemanticHandler {
    override fun canHandle(expression: Expr): Boolean = expression is BinaryExpression

    override fun analyze(
        expression: Expr,
        context: SemanticContext,
        analyzeChild: (Expr) -> ExpressionAnalysis,
    ): ExpressionAnalysis {
        val binary = expression as BinaryExpression
        val left = analyzeChild(binary.left)
        val right = analyzeChild(binary.right)
        val operation = operationTypeOrNull(binary.operator)
        val errors = mutableListOf<SemanticError>()

        if (binary.operator in BOOLEAN_OPERATORS) {
            validateBooleanOperands(binary, left, right, errors)
        } else if (operation == null) {
            errors.add(SemanticError(binary.position, "Unknown operator '${binary.operator}'"))
        } else {
            validateOperands(binary, operation, left, right, errors)
            validateDivisionByZero(binary, operation, right, errors)
        }

        errors.addAll(left.errors)
        errors.addAll(right.errors)

        return ExpressionAnalysis(
            type = inferType(binary.operator, operation, left.type, right.type),
            errors = errors,
            knownNumberValue = evaluateKnownNumber(operation, left, right),
        )
    }

    private fun operationTypeOrNull(operator: String): OperationType? =
        runCatching { OperationType.fromString(operator) }.getOrNull()

    private fun validateOperands(
        expression: BinaryExpression,
        operation: OperationType,
        left: ExpressionAnalysis,
        right: ExpressionAnalysis,
        errors: MutableList<SemanticError>,
    ) {
        if (operation != OperationType.PLUS && (left.type != "number" || right.type != "number")) {
            errors.add(SemanticError(expression.position, "Operator '${expression.operator}' requires operand"))
        }
    }

    private fun validateDivisionByZero(
        expression: BinaryExpression,
        operation: OperationType,
        right: ExpressionAnalysis,
        errors: MutableList<SemanticError>,
    ) {
        if (operation == OperationType.DIVIDE && errors.isEmpty() && right.knownNumberValue == 0.0) {
            errors.add(SemanticError(expression.position, "Division by zero"))
        }
    }

    private fun validateBooleanOperands(
        expression: BinaryExpression,
        left: ExpressionAnalysis,
        right: ExpressionAnalysis,
        errors: MutableList<SemanticError>,
    ) {
        if (left.type != "boolean" || right.type != "boolean") {
            errors.add(
                SemanticError(
                    expression.position,
                    "Operator '${expression.operator}' requires boolean operands",
                ),
            )
        }
    }

    private fun inferType(
        operator: String,
        operation: OperationType?,
        leftType: String?,
        rightType: String?,
    ): String? =
        when {
            operator in BOOLEAN_OPERATORS && leftType == "boolean" && rightType == "boolean" -> "boolean"
            operation == OperationType.PLUS && (leftType == "string" || rightType == "string") -> "string"
            leftType == "number" && rightType == "number" -> "number"
            else -> null
        }

    private fun evaluateKnownNumber(
        operation: OperationType?,
        left: ExpressionAnalysis,
        right: ExpressionAnalysis,
    ): Double? {
        val leftValue = left.knownNumberValue
        val rightValue = right.knownNumberValue

        // When muy concentrado, no implica gran complicacion si las operaciones crecen
        return if (leftValue != null && rightValue != null) {
            when (operation) {
                OperationType.PLUS -> leftValue + rightValue
                OperationType.MINUS -> leftValue - rightValue
                OperationType.MULTIPLY -> leftValue * rightValue
                OperationType.DIVIDE ->
                    if (rightValue == 0.0) {
                        null
                    } else {
                        leftValue / rightValue
                    }
                null -> null
            }
        } else {
            null
        }
    }

    private companion object {
        val BOOLEAN_OPERATORS = setOf("&&", "||")
    }
}
