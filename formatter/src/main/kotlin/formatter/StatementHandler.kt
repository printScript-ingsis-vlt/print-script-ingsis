package formatter

import ast.Expr
import ast.Position
import ast.Stmt

interface StatementHandler {
    fun canHandle(stmt: Stmt): Boolean

    @Suppress("LongParameterList")
    fun format(
        stmt: Stmt,
        indentationLevel: Int,
        rules: FormattingRules,
        formatExpression: (Expr) -> String,
        formatStatements: (List<Stmt>, Int) -> List<String>,
        indentation: (Int) -> String,
        originalGap: (Position, Int, Int) -> String,
    ): List<String>
}
