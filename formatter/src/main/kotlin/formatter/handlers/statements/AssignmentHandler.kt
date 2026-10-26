package formatter.handlers.statements

import ast.Assignment
import ast.Expr
import ast.Position
import ast.Stmt
import formatter.FormattingRules
import formatter.StatementHandler

class AssignmentHandler : StatementHandler {
    override fun canHandle(stmt: Stmt): Boolean = stmt is Assignment

    override fun format(
        stmt: Stmt,
        indentationLevel: Int,
        rules: FormattingRules,
        formatExpression: (Expr) -> String,
        formatStatements: (List<Stmt>, Int) -> List<String>,
        indentation: (Int) -> String,
        originalGap: (Position, Int, Int) -> String,
    ): List<String> {
        val assignment = stmt as Assignment
        val result =
            buildString {
                append(assignment.name)
                if (rules.spaceAroundEqual != null) {
                    if (rules.spaceAroundEqual) append(" = ") else append("=")
                } else {
                    append(originalGap(assignment.position, NAME_INDEX, EQUAL_INDEX))
                    append("=")
                    append(originalGap(assignment.position, EQUAL_INDEX, VALUE_START_INDEX))
                }
                append(formatExpression(assignment.value))
                append(";")
            }

        return listOf(indentation(indentationLevel) + result)
    }

    private companion object {
        const val NAME_INDEX = 0
        const val EQUAL_INDEX = 1
        const val VALUE_START_INDEX = 2
    }
}
