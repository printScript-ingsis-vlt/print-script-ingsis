package linter.handlers.expressions

import ast.BooleanLiteral
import ast.Expr
import linter.ExpressionHandler

class BooleanLiteralHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is BooleanLiteral
}
