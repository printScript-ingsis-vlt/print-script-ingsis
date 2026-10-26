package formatter.handlers.statements

import ast.Expr
import ast.IfStatement
import ast.Position
import ast.Stmt
import formatter.FormattingRules
import formatter.StatementHandler

class IfStatementHandler : StatementHandler {
    override fun canHandle(stmt: Stmt): Boolean = stmt is IfStatement

    override fun format(
        stmt: Stmt,
        indentationLevel: Int,
        rules: FormattingRules,
        formatExpression: (Expr) -> String,
        formatStatements: (List<Stmt>, Int) -> List<String>,
        indentation: (Int) -> String,
        originalGap: (Position, Int, Int) -> String,
    ): List<String> {
        val statement = stmt as IfStatement
        val currentIndentation = indentation(indentationLevel)
        val braceOnNewLine = rules.ifBraceOnNewLine == true
        val header =
            "${currentIndentation}if (${formatExpression(statement.condition)})" +
                if (braceOnNewLine) "" else " {"
        val lines = mutableListOf(header)
        if (braceOnNewLine) lines.add("$currentIndentation{")

        lines.addAll(formatStatements(statement.thenBranch, indentationLevel + 1))
        statement.elseBranch?.let { elseBranch ->
            if (braceOnNewLine) {
                lines.add("$currentIndentation}")
                lines.add("${currentIndentation}else")
                lines.add("$currentIndentation{")
            } else {
                lines.add("$currentIndentation} else {")
            }
            lines.addAll(formatStatements(elseBranch, indentationLevel + 1))
        }
        lines.add("$currentIndentation}")
        return lines
    }
}
