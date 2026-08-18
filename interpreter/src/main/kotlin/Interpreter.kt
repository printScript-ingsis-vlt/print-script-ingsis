import recurses.BinaryExpression
import recurses.Environment
import recurses.Expr
import recurses.Identifier
import recurses.NumberLiteral
import recurses.PrintStatement
import recurses.Program
import recurses.Stmt
import recurses.StringLiteral
import recurses.Variable
import recurses.VariableDeclaration
import valueDataclass.NumberValue
import valueDataclass.StringValue
import valueDataclass.Value


class Interpreter(private val output: Output) {

    private val environment = Environment()

    fun run(program: Program) {
        program.statements.forEach(::execute)
    }

    private fun execute(stmt: Stmt) = when (stmt) {
        is VariableDeclaration -> executeDeclaration(stmt)
        is PrintStatement -> executePrint(stmt)
    }

    private fun executeDeclaration(stmt: VariableDeclaration) {
        val value = stmt.value?.let(::evaluate)
        environment.declare(stmt.name, Variable(stmt.type, value))
    }

    private fun executePrint(stmt: PrintStatement) {
        val expr = evaluate(stmt.argument)
        output.write(expr.toString())
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
