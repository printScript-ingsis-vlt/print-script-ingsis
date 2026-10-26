package linter.handlers.expressions

import ast.Expr
import ast.Identifier
import linter.ExpressionHandler

class IdentifierHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is Identifier
}
