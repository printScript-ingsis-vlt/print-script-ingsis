package linter

import ast.Assignment
import ast.BinaryExpression
import ast.Identifier
import ast.IfStatement
import ast.Position
import ast.Program
import ast.ReadEnvExpression
import ast.ReadInputExpression
import ast.StringLiteral
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AstTraversalTest {
    @Test
    fun `visits expressions inside branches and compound expressions`() {
        val visitedExpressions = mutableListOf<String>()

        AstTraversal.forEachExpression(programWithNestedExpressions()) { expression ->
            visitedExpressions.add(expression::class.simpleName.orEmpty())
        }

        assertEquals(
            listOf(
                "Identifier",
                "BinaryExpression",
                "StringLiteral",
                "ReadInputExpression",
                "StringLiteral",
                "ReadEnvExpression",
                "StringLiteral",
            ),
            visitedExpressions,
        )
    }

    private fun programWithNestedExpressions(): Program {
        val position = Position(1, 1)
        val readInput = ReadInputExpression(StringLiteral("Enter message", position), position)
        val message = BinaryExpression(StringLiteral("Message: ", position), "+", readInput, position)
        val declaration = VariableDeclaration("message", "string", message, position)
        val readEnv = ReadEnvExpression(StringLiteral("MESSAGE", position), position)
        val assignment = Assignment("message", readEnv, position)
        val ifStatement =
            IfStatement(
                Identifier("enabled", position),
                listOf(declaration),
                listOf(assignment),
                position,
            )

        return Program(position, listOf(ifStatement))
    }
}
