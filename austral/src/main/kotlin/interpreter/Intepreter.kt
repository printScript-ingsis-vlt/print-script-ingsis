package austral.src.main.kotlin.interpreter

import austral.src.main.kotlin.commun.*
import austral.src.main.kotlin.commun.result.*

class Interpreter(private val output: Output) { // --> El output es el destino
    private val environment = Environment()

    fun run(program: Program): Result<Unit, List<CompilerError>> =
        try {
            program.statements.forEach(::execute)
            Result.Success(Unit)
        } catch (e: InterpreterException) {
            Result.Failure(listOf(e.error))
        } // --> Interpreta el arbol recorriendolo statement a statement, y ejecutando

    private fun execute(stmt: Stmt) = when (stmt) {
        is VariableDeclaration -> executeDeclaration(stmt)
    } // --> Si es el statemente es un VD, ejecuta algo

    private fun executeDeclaration(stmt: VariableDeclaration) {
        if (stmt.type != "number" && stmt.type != "string")
            throw error(stmt, "Invalid type '${stmt.type}'")
        val value = stmt.value?.let(::evaluate)
        if (value != null && value.type != stmt.type)
            throw error(stmt, "Cannot assign a value of type '${value.type}' to a variable of type '${stmt.type}'")
        environment.declare(stmt.name, Variable(stmt.type, value))
    } // -->  Si no es valido tiro error, evaluo si hay valor y si coinciden los tipos

    private fun evaluate(expr: Expr): Value = when (expr) {
        is NumberLiteral -> NumberValue(expr.value)
        is StringLiteral -> StringValue(expr.value)
        is Identifier -> { // --> "x" por ejemplo
            val variable = environment.lookup(expr.name)
                ?: throw error(expr, "Variable '${expr.name}' is not declared")
            variable.value ?: throw error(expr, "Variable '${expr.name}' is not initialized")
        } // --> Chequeo si se definio la variable y si se le asigno su respectivo valor
        is BinaryExpression -> {
            val left = evaluate(expr.left)
            val right = evaluate(expr.right)
            when {
                expr.operator == "+" && (left is StringValue || right is StringValue) ->
                    StringValue(left.asString() + right.asString()) // --> Suma de strings
                expr.operator !in setOf("+", "-", "*", "/") ->
                    throw error(expr, "Unknown operator '${expr.operator}'") // --> Operador valido
                left !is NumberValue || right !is NumberValue ->
                    throw error(expr, "Operator '${expr.operator}' requires numeric operands") // --> Suma de tipos valida
                expr.operator == "/" && right.value == 0.0 ->
                    throw error(expr, "Division by zero")
                else -> NumberValue(
                    when (expr.operator) {
                        "+" -> left.value + right.value
                        "-" -> left.value - right.value
                        "*" -> left.value * right.value
                        else -> left.value / right.value
                    }
                )
            }
        }
    }

    private class InterpreterException(val error: SemanticError) : RuntimeException()

    private fun error(node: Node, message: String): InterpreterException =
        InterpreterException(SemanticError(node.position, message))
}