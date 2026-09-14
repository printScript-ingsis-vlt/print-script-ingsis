package semantic

import ast.Assignment
import ast.Identifier
import ast.NumberLiteral
import ast.PrintStatement
import ast.Program
import ast.StringLiteral
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SemanticAnalyzerTest {
    private val analyzer = SemanticAnalyzer()

    @Test
    fun `analyzer updates number variables after declarations and assignments`() {
        val program =
            Program(
                pos(),
                listOf(
                    VariableDeclaration("count", "number", NumberLiteral(1.0, pos(1)), pos(1)),
                    Assignment("count", NumberLiteral(2.0, pos(2)), pos(2)),
                    PrintStatement(Identifier("count", pos(3)), pos(3)),
                ),
            )

        assertTrue(analyzer.analyze(program).isEmpty())
    }

    @Test
    fun `analyzer updates string variables after declarations and assignments`() {
        val program =
            Program(
                pos(),
                listOf(
                    VariableDeclaration("message", "string", StringLiteral("hi", pos(1)), pos(1)),
                    Assignment("message", StringLiteral("bye", pos(2)), pos(2)),
                    PrintStatement(Identifier("message", pos(3)), pos(3)),
                ),
            )

        assertTrue(analyzer.analyze(program).isEmpty())
    }

    @Test
    fun `analyzer does not declare variables when their declaration has errors`() {
        val program =
            Program(
                pos(),
                listOf(
                    VariableDeclaration("flag", "boolean", null, pos(1)),
                    PrintStatement(Identifier("flag", pos(2)), pos(2)),
                ),
            )

        val errors = analyzer.analyze(program)

        assertEquals(2, errors.size)
        assertEquals("Invalid type 'boolean'", errors[0].message)
        assertEquals("Variable 'flag' is not declared", errors[1].message)
    }
}
