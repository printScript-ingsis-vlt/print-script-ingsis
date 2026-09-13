package handlers.expressions

import ast.BinaryExpression
import ast.Expr
import interpreter.ExpressionHandler
import runtime.Environment
import runtime.OperationType
import runtime.valuedataclass.NumberValue
import runtime.valuedataclass.StringValue
import runtime.valuedataclass.Value

class BinaryExpressionHandler : ExpressionHandler {
    override fun canHandle(expr: Expr) = expr is BinaryExpression

    override fun evaluate(
        expr: Expr,
        environment: Environment,
        evaluate: (Expr) -> Value,
    ): Value {
        val binary = expr as BinaryExpression
        val left = evaluate(binary.left)
        val right = evaluate(binary.right)
        val op = OperationType.fromString(binary.operator)

        return when {
            // Concatenación de strings
            op == OperationType.PLUS && (left is StringValue || right is StringValue) -> {
                StringValue(left.asString() + right.asString())
            }

            // Operaciones numéricas
            else -> {
                val leftNum =
                    (left as? NumberValue)?.value
                        ?: error("Left operand must be a number")
                val rightNum =
                    (right as? NumberValue)?.value
                        ?: error("Right operand must be a number")

                NumberValue(
                    when (op) {
                        OperationType.PLUS -> leftNum + rightNum
                        OperationType.MINUS -> leftNum - rightNum
                        OperationType.MULTIPLY -> leftNum * rightNum
                        OperationType.DIVIDE -> leftNum / rightNum
                        else -> error("Unsupported operator: ${binary.operator}")
                    },
                )
            }
        }
    }
}
