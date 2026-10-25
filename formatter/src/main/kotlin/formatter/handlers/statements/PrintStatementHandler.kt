package formatter.handlers.statements

import ast.Expr
import ast.Position
import ast.PrintStatement
import ast.Stmt
import formatter.FormattingRules
import formatter.StatementHandler

class PrintStatementHandler : StatementHandler {
    override fun canHandle(stmt: Stmt): Boolean = stmt is PrintStatement

    override fun format(
        stmt: Stmt,
        indentationLevel: Int,
        rules: FormattingRules,
        formatExpression: (Expr) -> String,
        formatStatements: (List<Stmt>, Int) -> List<String>,
        indentation: (Int) -> String,
        originalGap: (Position, Int, Int) -> String,
    ): List<String> {
        val statement = stmt as PrintStatement
        val result =
            if (rules.spaceAroundPrintParens == true) {
                "println ( ${formatExpression(statement.argument)} );"
            } else {
                "println(${formatExpression(statement.argument)});"
            }
        return listOf(indentation(indentationLevel) + result)
    }
}
