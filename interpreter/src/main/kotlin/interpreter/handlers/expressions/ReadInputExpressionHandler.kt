package interpreter.handlers.expressions

import ast.Expr
import ast.ReadInputExpression
import interpreter.ExecutionContext
import interpreter.ExpressionHandler
import runtime.valuedataclass.BooleanValue
import runtime.valuedataclass.NumberValue
import runtime.valuedataclass.StringValue
import runtime.valuedataclass.Value

class ReadInputExpressionHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is ReadInputExpression

    override fun evaluate(
        expr: Expr,
        context: ExecutionContext,
        evaluate: (Expr) -> Value,
    ): Value {
        val readInputExpr = expr as ReadInputExpression

        val promptValue = evaluate(readInputExpr.prompt)
        val promptText =
            when (promptValue) {
                is StringValue -> promptValue.value
                else -> promptValue.toString()
            }

        if (promptText.isNotEmpty()) {
            context.output.write(promptText)
        }

        val rawInput = context.inputProvider.readLine()
        return parseInputValue(rawInput)
    }

    private fun parseInputValue(input: String): Value {
        val number = input.toDoubleOrNull()
        return when {
            number != null -> NumberValue(number)
            input.equals("true", ignoreCase = true) -> BooleanValue(true)
            input.equals("false", ignoreCase = true) -> BooleanValue(false)
            else -> StringValue(input)
        }
    }
}
