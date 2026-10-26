package formatter

import ast.Expr
import ast.Position
import ast.PrintStatement
import ast.Program
import ast.Stmt
import token.Token

/**
 * Formateador para PrintScript.
 *
 * Implementa la interfaz Formatter y aplica reglas de formato al AST.
 */
class PrintScriptFormatter(
    private val rules: FormattingRules = FormattingRules.default(),
    private val configuration: FormatterConfiguration = FormatterConfigurations.default,
) : Formatter {
    // seteado al arrancar cada format(): permite ir a buscar el espaciado real del
    // archivo original cuando una regla viene en null
    private lateinit var tokens: List<Token>

    override fun format(
        tokens: List<Token>,
        program: Program,
    ): String {
        this.tokens = tokens
        val lines = formatStatements(program.statements, indentationLevel = 0)
        return lines.joinToString("\n")
    }

    // espacio real que había en el archivo original entre dos tokens de un statement,
    // ubicados por su offset respecto al primer token del statement (ej. el 'let'/'const'
    // de una declaracion, o el nombre en una asignacion)
    private fun originalGap(
        position: Position,
        beforeIndex: Int,
        afterIndex: Int,
    ): String {
        val index = tokens.indexOfFirst { it.start == position }
        require(index >= 0) { "No se encontró un token en la posición $position" }
        val before = tokens[index + beforeIndex]
        val after = tokens[index + afterIndex]
        if (before.end.line != after.start.line) return ""
        return " ".repeat((after.start.column - before.end.column).coerceAtLeast(0))
    }

    // permite cualquier lista de stmt, ya sea de un bloque como de un programa entero
    private fun formatStatements(
        statements: List<Stmt>,
        indentationLevel: Int,
    ): List<String> {
        val lines = mutableListOf<String>()

        val newlinesBeforePrintln = rules.newlinesBeforePrintln ?: FormattingRules.DEFAULT_NEWLINES_BEFORE_PRINTLN
        for ((index, stmt) in statements.withIndex()) {
            // Solo separa un println de OTRO println anterior, no de cualquier statement.
            val followsAnotherPrintln = index > 0 && stmt is PrintStatement && statements[index - 1] is PrintStatement
            if (followsAnotherPrintln && newlinesBeforePrintln > 0) {
                repeat(newlinesBeforePrintln) { lines.add("") }
            }
            lines.addAll(formatStatement(stmt, indentationLevel))
        }

        return lines
    }

    private fun formatStatement(
        stmt: Stmt,
        indentationLevel: Int,
    ): List<String> {
        val handler =
            configuration.statementHandlers.find { it.canHandle(stmt) }
                ?: throw IllegalArgumentException("Unknown statement type: ${stmt.javaClass.simpleName}")
        return handler.format(
            stmt,
            indentationLevel,
            rules,
            ::formatExpression,
            ::formatStatements,
            ::indentation,
            ::originalGap,
        )
    }

    private fun indentation(level: Int): String {
        require(level >= 0) { "Indentation level cannot be negative" }
        return " ".repeat(level * (rules.indentationSpaces ?: FormattingRules.DEFAULT_INDENTATION_SPACES))
    }

    /**
     * Formatea una expresión.
     */
    private fun formatExpression(expr: Expr): String {
        val handler =
            configuration.expressionHandlers.find { it.canHandle(expr) }
                ?: throw IllegalArgumentException("Unknown expression type: ${expr.javaClass.simpleName}")
        return handler.format(expr, ::formatExpression)
    }
}
