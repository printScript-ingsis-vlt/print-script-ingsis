package linter.handlers.expressions

import ast.Expr
import ast.NumberLiteral
import linter.ExpressionHandler

class NumberLiteralHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is NumberLiteral
}
