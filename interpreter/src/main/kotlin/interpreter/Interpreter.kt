package interpreter

import ast.Assignment
import ast.BinaryExpression
import ast.Expr
import ast.Identifier
import ast.NumberLiteral
import ast.PrintStatement
import ast.Program
import ast.Stmt
import ast.StringLiteral
import ast.VariableDeclaration
import runtime.Environment
import runtime.OperationType
import runtime.Variable
import runtime.valuedataclass.NumberValue
import runtime.valuedataclass.StringValue
import runtime.valuedataclass.Value

class Interpreter(private val output: Output) {
    private val environment = Environment()

    fun run(program: Program) {
        program.statements.forEach(::execute)
    }

    private fun execute(stmt: Stmt) =
        when (stmt) {
            is VariableDeclaration -> executeDeclaration(stmt)
            is Assignment -> executeAssignment(stmt)
            is PrintStatement -> executePrint(stmt)
        }

    private fun executeDeclaration(stmt: VariableDeclaration) {
        val value = stmt.value?.let(::evaluate)
        environment.declare(stmt.name, Variable(stmt.type, value))
    }

    private fun executeAssignment(stmt: Assignment) {
        val value = evaluate(stmt.value)
        environment.assign(stmt.name, value)
    }

    private fun executePrint(stmt: PrintStatement) {
        val expr = evaluate(stmt.argument)
        output.write(expr.asString())
    }

    fun evaluate(expr: Expr): Value =
        when (expr) {
            is NumberLiteral -> NumberValue(expr.value)
            is StringLiteral -> StringValue(expr.value)
            is Identifier -> environment.lookup(expr.name)!!.value!!
            is BinaryExpression -> {
                val left = evaluate(expr.left)
                val right = evaluate(expr.right)
                val operationType = OperationType.fromString(expr.operator)
                when {
                    operationType == OperationType.PLUS && (left is StringValue || right is StringValue) ->
                        StringValue(left.asString() + right.asString())
                    else ->
                        NumberValue(
                            when (operationType) {
                                OperationType.PLUS -> (left as NumberValue).value + (right as NumberValue).value
                                OperationType.MINUS -> (left as NumberValue).value - (right as NumberValue).value
                                OperationType.MULTIPLY -> (left as NumberValue).value * (right as NumberValue).value
                                OperationType.DIVIDE -> (left as NumberValue).value / (right as NumberValue).value
                            },
                        )
                }
            }
        }
}
