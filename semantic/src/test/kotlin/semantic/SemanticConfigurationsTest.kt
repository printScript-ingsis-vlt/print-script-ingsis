package semantic

import ast.BooleanLiteral
import ast.IfStatement
import ast.NumberLiteral
import ast.PrintStatement
import ast.Program
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SemanticConfigurationsTest {
    @Test
    fun `v1 0 supports the current language handlers`() {
        val errors = SemanticAnalyzer(SemanticConfigurations.v1_0).analyze(validV10Program())

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `v1 1 validates boolean literals`() {
        val program = Program(pos(), listOf(PrintStatement(BooleanLiteral(true, pos()), pos())))
        val errors = SemanticAnalyzer(SemanticConfigurations.v1_1).analyze(program)

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `v1 0 rejects constants`() {
        val program =
            Program(
                pos(),
                listOf(
                    VariableDeclaration(
                        name = "limit",
                        type = "number",
                        value = NumberLiteral(10.0, pos()),
                        position = pos(),
                        mutable = false,
                    ),
                ),
            )

        val errors = SemanticAnalyzer(SemanticConfigurations.v1_0).analyze(program)

        assertEquals("Constants are not supported in this language version", errors.single().message)
    }

    @Test
    fun `v1 1 accepts constants`() {
        val program =
            Program(
                pos(),
                listOf(
                    VariableDeclaration(
                        name = "limit",
                        type = "number",
                        value = NumberLiteral(10.0, pos()),
                        position = pos(),
                        mutable = false,
                    ),
                ),
            )

        val errors = SemanticAnalyzer(SemanticConfigurations.v1_1).analyze(program)

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `v1 0 does not register the boolean literal handler`() {
        val program = Program(pos(), listOf(PrintStatement(BooleanLiteral(true, pos()), pos())))

        val exception =
            assertThrows(IllegalStateException::class.java) {
                SemanticAnalyzer(SemanticConfigurations.v1_0).analyze(program)
            }

        assertEquals("No semantic expression handler found for: BooleanLiteral", exception.message)
    }

    @Test
    fun `v1 0 does not register the if statement handler`() {
        val program = Program(pos(), listOf(IfStatement(BooleanLiteral(true, pos()), emptyList(), null, pos())))

        val exception =
            assertThrows(IllegalStateException::class.java) {
                SemanticAnalyzer(SemanticConfigurations.v1_0).analyze(program)
            }

        assertEquals("No semantic statement handler found for: IfStatement", exception.message)
    }

    private fun validV10Program(): Program =
        Program(
            pos(),
            listOf(
                VariableDeclaration("count", "number", NumberLiteral(1.0, pos()), pos()),
            ),
        )
}
