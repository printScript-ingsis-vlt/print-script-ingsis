package formatter

import ast.Assignment
import ast.BinaryExpression
import ast.BooleanLiteral
import ast.Expr
import ast.Identifier
import ast.IfStatement
import ast.NumberLiteral
import ast.PrintStatement
import ast.Program
import ast.ReadEnvExpression
import ast.ReadInputExpression
import ast.Stmt
import ast.StringLiteral
import ast.VariableDeclaration

/**
 * Formateador para PrintScript.
 *
 * Implementa la interfaz Formatter y aplica reglas de formato al AST.
 * Soporta: declarations, assignments, print statements, binary expressions.
 */
class PrintScriptFormatter(
    private val rules: FormattingRules = FormattingRules.default(),
) : Formatter {
    override fun format(program: Program): String {
        val lines = formatStatements(program.statements, indentationLevel = 0)
        return lines.joinToString("\n")
    }

    // permite cualquier lista de stmt, ya sea de un bloque como de un programa entero
    private fun formatStatements(
        statements: List<Stmt>,
        indentationLevel: Int,
    ): List<String> {
        val lines = mutableListOf<String>()

        for ((index, stmt) in statements.withIndex()) {
            if (index > 0 && stmt is PrintStatement && rules.newlinesBeforePrintln > 0) {
                repeat(rules.newlinesBeforePrintln) { lines.add("") }
            }
            lines.addAll(formatStatement(stmt, indentationLevel))
        }

        return lines
    }

    private fun formatStatement(
        stmt: Stmt,
        indentationLevel: Int,
    ): List<String> {
        if (stmt is IfStatement) {
            return formatIfStatement(stmt, indentationLevel)
        }

        val formattedStatement =
            when (stmt) {
                is VariableDeclaration -> formatVariableDeclaration(stmt)
                is Assignment -> formatAssignment(stmt)
                is PrintStatement -> formatPrintStatement(stmt)
                else -> throw IllegalArgumentException("Unknown statement type: ${stmt.javaClass.simpleName}")
            }

        return listOf(indentation(indentationLevel) + formattedStatement)
    }

    // A partir del nivel del if, devuelve todas sus líneas formateadas.
    private fun formatIfStatement(
        statement: IfStatement,
        indentationLevel: Int,
    ): List<String> {
        val indentation = indentation(indentationLevel)
        val lines = mutableListOf("${indentation}if (${formatExpression(statement.condition)}) {")

        lines.addAll(formatStatements(statement.thenBranch, indentationLevel + 1))
        statement.elseBranch?.let { elseBranch ->
            lines.add("$indentation} else {")
            lines.addAll(formatStatements(elseBranch, indentationLevel + 1))
        }
        lines.add("$indentation}")

        return lines
    }

    private fun indentation(level: Int): String {
        require(level >= 0) { "Indentation level cannot be negative" }
        return " ".repeat(level * rules.indentationSpaces)
    }

    /**
     * Formatea una declaración de variable.
     * Ejemplo: let name: string = "Joe"; o const name: string = "Joe";
     */
    private fun formatVariableDeclaration(stmt: VariableDeclaration): String {
        val sb = StringBuilder()

        sb.append(if (stmt.mutable) "let" else "const")
        sb.append(" ")
        sb.append(stmt.name)

        // Espacio alrededor de ':'
        if (rules.spaceBeforeColon) sb.append(" ")
        sb.append(":")
        if (rules.spaceAfterColon) sb.append(" ")

        sb.append(stmt.type)

        // Valor (si existe)
        stmt.value?.let { value ->
            if (rules.spaceAroundEqual) {
                sb.append(" = ")
            } else {
                sb.append("=")
            }
            sb.append(formatExpression(value))
        }

        sb.append(";")

        return sb.toString()
    }

    /**
     * Formatea una asignación de variable.
     * Ejemplo: x = 10;
     */
    private fun formatAssignment(stmt: Assignment): String {
        val sb = StringBuilder()

        sb.append(stmt.name)

        if (rules.spaceAroundEqual) {
            sb.append(" = ")
        } else {
            sb.append("=")
        }

        sb.append(formatExpression(stmt.value))
        sb.append(";")

        return sb.toString()
    }

    /**
     * Formatea un statement de println.
     * Ejemplo: println(x);
     */
    private fun formatPrintStatement(stmt: PrintStatement): String {
        val sb = StringBuilder()
        sb.append("println(")
        sb.append(formatExpression(stmt.argument))
        sb.append(");")
        return sb.toString()
    }

    /**
     * Formatea una expresión.
     */
    private fun formatExpression(expr: Expr): String {
        return when (expr) {
            is NumberLiteral -> expr.value.toString()
            is StringLiteral -> "\"${expr.value}\""
            is BooleanLiteral -> expr.value.toString()
            is Identifier -> expr.name
            is BinaryExpression -> formatBinaryExpression(expr)
            is ReadEnvExpression -> TODO()
            is ReadInputExpression -> TODO()
        }
    }

    /**
     * Formatea una expresión binaria.
     * Ejemplo: a + b, a * c, "hello" + name, etc.
     */
    private fun formatBinaryExpression(expr: BinaryExpression): String {
        val sb = StringBuilder()

        sb.append(formatExpression(expr.left))

        // Espacio alrededor de operadores
        if (FormattingRules.SPACE_AROUND_OPERATORS) {
            sb.append(" ")
            sb.append(expr.operator)
            sb.append(" ")
        } else {
            sb.append(expr.operator)
        }

        sb.append(formatExpression(expr.right))

        return sb.toString()
    }
}
