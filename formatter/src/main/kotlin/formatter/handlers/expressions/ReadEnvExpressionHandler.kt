package formatter.handlers.expressions

import ast.Expr
import ast.ReadEnvExpression
import formatter.ExpressionHandler

class ReadEnvExpressionHandler : ExpressionHandler {
    override fun canHandle(expr: Expr): Boolean = expr is ReadEnvExpression

    override fun format(
        expr: Expr,
        formatExpression: (Expr) -> String,
    ): String = "readEnv(${formatExpression((expr as ReadEnvExpression).envVariableName)})"
}
