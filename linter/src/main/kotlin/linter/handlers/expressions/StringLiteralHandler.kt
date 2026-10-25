package linter.handlers.expressions

import ast.Expr
import ast.StringLiteral
import linter.ExpressionHandler

class StringLiteralHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is StringLiteral
}
