package austral.src.main.kotlin.interpreter

import austral.src.main.kotlin.commun.*
import austral.src.main.kotlin.commun.ast.BinaryExpression
import austral.src.main.kotlin.commun.ast.Expr
import austral.src.main.kotlin.commun.ast.Identifier
import austral.src.main.kotlin.commun.ast.NumberLiteral
import austral.src.main.kotlin.commun.ast.Program
import austral.src.main.kotlin.commun.ast.Stmt
import austral.src.main.kotlin.commun.ast.StringLiteral
import austral.src.main.kotlin.commun.ast.VariableDeclaration

class Interpreter(private val output: Output) {

    private val environment = Environment()

    fun run(program: Program) {
        program.statements.forEach(::execute)
    }

    private fun execute(stmt: Stmt) = when (stmt) {
        is VariableDeclaration -> executeDeclaration(stmt)
    }

    private fun executeDeclaration(stmt: VariableDeclaration) {
        val value = stmt.value?.let(::evaluate)
        environment.declare(stmt.name, Variable(stmt.type, value))
    }

    fun evaluate(expr: Expr): Value = when (expr) {
        is NumberLiteral -> NumberValue(expr.value)
        is StringLiteral -> StringValue(expr.value)
        is Identifier -> environment.lookup(expr.name)!!.value!!
        is BinaryExpression -> {
            val left = evaluate(expr.left)
            val right = evaluate(expr.right)
            when {
                expr.operator == "+" && (left is StringValue || right is StringValue) ->
                    StringValue(left.asString() + right.asString())
                else -> NumberValue(
                    when (expr.operator) {
                        "+" -> (left as NumberValue).value + (right as NumberValue).value
                        "-" -> (left as NumberValue).value - (right as NumberValue).value
                        "*" -> (left as NumberValue).value * (right as NumberValue).value
                        else -> (left as NumberValue).value / (right as NumberValue).value
                    }
                )
            }
        }
    }
}