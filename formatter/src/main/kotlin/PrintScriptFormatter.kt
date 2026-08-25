import recurses.Assignment
import recurses.BinaryExpression
import recurses.Expr
import recurses.Identifier
import recurses.NumberLiteral
import recurses.PrintStatement
import recurses.Program
import recurses.Stmt
import recurses.StringLiteral
import recurses.VariableDeclaration

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
        val lines = mutableListOf<String>()

        for ((index, stmt) in program.statements.withIndex()) {
            if (index > 0 && stmt is PrintStatement && rules.newlinesBeforePrintln > 0) {
                repeat(rules.newlinesBeforePrintln) { lines.add("") }
            }
            lines.add(formatStatement(stmt))
        }

        return lines.joinToString("\n")
    }

    private fun formatStatement(stmt: Stmt): String {
        return when (stmt) {
            is VariableDeclaration -> formatVariableDeclaration(stmt)
            is Assignment -> formatAssignment(stmt)
            is PrintStatement -> formatPrintStatement(stmt)
            else -> throw IllegalArgumentException("Unknown statement type: ${stmt.javaClass.simpleName}")
        }
    }

    /**
     * Formatea una declaración de variable.
     * Ejemplo: let name: string = "Joe";
     */
    private fun formatVariableDeclaration(stmt: VariableDeclaration): String {
        val sb = StringBuilder()

        sb.append("let")
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
            is Identifier -> expr.name
            is BinaryExpression -> formatBinaryExpression(expr)
            else -> throw IllegalArgumentException("Unknown expression type: ${expr.javaClass.simpleName}")
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
