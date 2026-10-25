package formatter.handlers.statements

import ast.Expr
import ast.Position
import ast.Stmt
import ast.VariableDeclaration
import formatter.FormattingRules
import formatter.StatementHandler

class VariableDeclarationHandler : StatementHandler {
    override fun canHandle(stmt: Stmt): Boolean = stmt is VariableDeclaration

    override fun format(
        stmt: Stmt,
        indentationLevel: Int,
        rules: FormattingRules,
        formatExpression: (Expr) -> String,
        formatStatements: (List<Stmt>, Int) -> List<String>,
        indentation: (Int) -> String,
        originalGap: (Position, Int, Int) -> String,
    ): List<String> {
        val declaration = stmt as VariableDeclaration
        val result =
            buildString {
                append(if (declaration.mutable) "let" else "const")
                append(" ")
                append(declaration.name)

                if (rules.spaceBeforeColon != null) {
                    if (rules.spaceBeforeColon) append(" ")
                } else {
                    append(originalGap(declaration.position, NAME_INDEX, COLON_INDEX))
                }
                append(":")
                if (rules.spaceAfterColon != null) {
                    if (rules.spaceAfterColon) append(" ")
                } else {
                    append(originalGap(declaration.position, COLON_INDEX, TYPE_INDEX))
                }
                append(declaration.type)

                declaration.value?.let { value ->
                    if (rules.spaceAroundEqual != null) {
                        if (rules.spaceAroundEqual) append(" = ") else append("=")
                    } else {
                        append(originalGap(declaration.position, TYPE_INDEX, EQUAL_INDEX))
                        append("=")
                        append(originalGap(declaration.position, EQUAL_INDEX, VALUE_START_INDEX))
                    }
                    append(formatExpression(value))
                }
                append(";")
            }

        return listOf(indentation(indentationLevel) + result)
    }

    private companion object {
        const val NAME_INDEX = 1
        const val COLON_INDEX = 2
        const val TYPE_INDEX = 3
        const val EQUAL_INDEX = 4
        const val VALUE_START_INDEX = 5
    }
}
