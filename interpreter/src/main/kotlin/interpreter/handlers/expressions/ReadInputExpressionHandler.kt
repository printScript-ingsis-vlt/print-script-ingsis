package interpreter.handlers.expressions

import ast.Expr
import ast.ReadInputExpression
import interpreter.ExpressionHandler
import interpreter.InputProvider
import interpreter.Output
import runtime.Environment
import runtime.valuedataclass.BooleanValue
import runtime.valuedataclass.NumberValue
import runtime.valuedataclass.StringValue
import runtime.valuedataclass.Value

class ReadInputExpressionHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is ReadInputExpression

    override fun evaluate(
        expr: Expr,
        environment: Environment,
        evaluate: (Expr) -> Value,
        inputProvider: InputProvider,
        output: Output,
    ): Value {
        val readInputExpr = expr as ReadInputExpression

        val promptValue = evaluate(readInputExpr.prompt)
        val promptText =
            when (promptValue) {
                is StringValue -> promptValue.value
                else -> promptValue.toString()
            }

        if (promptText.isNotEmpty()) {
            output.write(promptText)
        }

        val rawInput = inputProvider.readLine()
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
