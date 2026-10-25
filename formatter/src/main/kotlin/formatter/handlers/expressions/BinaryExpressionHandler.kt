package formatter.handlers.expressions

import ast.BinaryExpression
import ast.Expr
import formatter.ExpressionHandler
import formatter.FormattingRules

class BinaryExpressionHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is BinaryExpression

    override fun format(
        expr: Expr,
        formatExpression: (Expr) -> String,
    ): String {
        val binary = expr as BinaryExpression
        val operator = if (FormattingRules.SPACE_AROUND_OPERATORS) " ${binary.operator} " else binary.operator
        return formatExpression(binary.left) + operator + formatExpression(binary.right)
    }
}
