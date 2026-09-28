package formatter

import ast.Assignment
import ast.BinaryExpression
import ast.BooleanLiteral
import ast.Expr
import ast.Identifier
import ast.IfStatement
import ast.NumberLiteral
import ast.Position
import ast.PrintStatement
import ast.Program
import ast.ReadEnvExpression
import ast.ReadInputExpression
import ast.Stmt
import ast.StringLiteral
import ast.VariableDeclaration
import token.Token

/**
 * Formateador para PrintScript.
 *
 * Implementa la interfaz Formatter y aplica reglas de formato al AST.
 */
class PrintScriptFormatter(
    private val rules: FormattingRules = FormattingRules.default(),
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
            if (index > 0 && stmt is PrintStatement && statements[index - 1] is PrintStatement && newlinesBeforePrintln > 0) {
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
        if (stmt is IfStatement) {
            return formatIfStatement(stmt, indentationLevel)
        }

        val formattedStatement =
            when (stmt) {
                is VariableDeclaration -> formatVariableDeclaration(stmt)
                is Assignment -> formatAssignment(stmt)
                is PrintStatement -> "println(${formatExpression(stmt.argument)});"
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
        return " ".repeat(level * (rules.indentationSpaces ?: FormattingRules.DEFAULT_INDENTATION_SPACES))
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

        // tokens: [let/const, nombre, ':', tipo, ('=', ...valor..., ';') | ';']

        // Espacio antes de ':'
        if (rules.spaceBeforeColon != null) {
            if (rules.spaceBeforeColon) sb.append(" ")
        } else {
            sb.append(originalGap(stmt.position, NAME_INDEX, COLON_INDEX))
        }
        sb.append(":")
        // Espacio despues de ':'
        if (rules.spaceAfterColon != null) {
            if (rules.spaceAfterColon) sb.append(" ")
        } else {
            sb.append(originalGap(stmt.position, COLON_INDEX, TYPE_INDEX))
        }

        sb.append(stmt.type)

        // Valor (si existe)
        stmt.value?.let { value ->
            if (rules.spaceAroundEqual != null) {
                if (rules.spaceAroundEqual) sb.append(" = ") else sb.append("=")
            } else {
                sb.append(originalGap(stmt.position, TYPE_INDEX, EQUAL_INDEX))
                sb.append("=")
                sb.append(originalGap(stmt.position, EQUAL_INDEX, VALUE_START_INDEX))
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

        if (rules.spaceAroundEqual != null) {
            if (rules.spaceAroundEqual) sb.append(" = ") else sb.append("=")
        } else {
            // tokens: [nombre, '=', ...valor..., ';']
            sb.append(originalGap(stmt.position, ASSIGN_NAME_INDEX, ASSIGN_EQUAL_INDEX))
            sb.append("=")
            sb.append(originalGap(stmt.position, ASSIGN_EQUAL_INDEX, ASSIGN_VALUE_START_INDEX))
        }

        sb.append(formatExpression(stmt.value))
        sb.append(";")

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
            is ReadEnvExpression -> "readEnv(${formatExpression(expr.envVariableName)})"
            is ReadInputExpression -> "readInput(${formatExpression(expr.prompt)})"
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

    private companion object {
        // offsets desde el token de posicion de una VariableDeclaration: [let/const, nombre, ':', tipo, ...]
        const val NAME_INDEX = 1
        const val COLON_INDEX = 2
        const val TYPE_INDEX = 3
        const val EQUAL_INDEX = 4
        const val VALUE_START_INDEX = 5

        // offsets desde el token de posicion de un Assignment: [nombre, '=', ...valor...]
        const val ASSIGN_NAME_INDEX = 0
        const val ASSIGN_EQUAL_INDEX = 1
        const val ASSIGN_VALUE_START_INDEX = 2
    }
}
