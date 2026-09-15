package interpreter.handlers.expressions

import ast.Expr
import ast.NumberLiteral
import interpreter.ExpressionHandler
import interpreter.InputProvider
import interpreter.Output
import runtime.Environment
import runtime.valuedataclass.NumberValue
import runtime.valuedataclass.Value

class NumberLiteralHandler : ExpressionHandler {
    override fun canHandle(expr: Expr) = expr is NumberLiteral

    override fun evaluate(
        expr: Expr,
        environment: Environment,
        evaluate: (Expr) -> Value,
        inputProvider: InputProvider,
        output: Output,
    ): Value {
        val literal = expr as NumberLiteral
        return NumberValue(literal.value)
    }
}
