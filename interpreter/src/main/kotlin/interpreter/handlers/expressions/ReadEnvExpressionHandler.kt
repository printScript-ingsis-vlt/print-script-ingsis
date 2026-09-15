package interpreter.handlers.expressions

import ast.Expr
import ast.ReadEnvExpression
import interpreter.ExecutionContext
import interpreter.ExpressionHandler
import runtime.valuedataclass.BooleanValue
import runtime.valuedataclass.NumberValue
import runtime.valuedataclass.StringValue
import runtime.valuedataclass.Value

class ReadEnvExpressionHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is ReadEnvExpression

    override fun evaluate(
        expr: Expr,
        context: ExecutionContext,
        evaluate: (Expr) -> Value,
    ): Value {
        val readEnvExpr = expr as ReadEnvExpression
        val envNameValue = evaluate(readEnvExpr.envVariableName)

        val envName =
            when (envNameValue) {
                is StringValue -> envNameValue.value
                else -> envNameValue.toString()
            }

        val rawEnvValue =
            context.envProvider.getEnv(envName)
                ?: error("Environment variable '$envName' is not defined")

        return parseEnvValue(rawEnvValue)
    }

    private fun parseEnvValue(value: String): Value {
        val number = value.toDoubleOrNull()
        return when {
            number != null -> NumberValue(number)
            value.equals("true", ignoreCase = true) -> BooleanValue(true)
            value.equals("false", ignoreCase = true) -> BooleanValue(false)
            else -> StringValue(value)
        }
    }
}
