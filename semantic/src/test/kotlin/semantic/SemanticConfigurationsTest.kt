package semantic

import ast.NumberLiteral
import ast.Program
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SemanticConfigurationsTest {
    @Test
    fun `v1 0 supports the current language handlers`() {
        val errors = SemanticAnalyzer(SemanticConfigurations.v1_0).analyze(validV10Program())

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `v1 1 is independently selectable while it awaits new AST nodes`() {
        val errors = SemanticAnalyzer(SemanticConfigurations.v1_1).analyze(validV10Program())

        assertTrue(errors.isEmpty())
    }

    private fun validV10Program(): Program =
        Program(
            pos(),
            listOf(
                VariableDeclaration("count", "number", NumberLiteral(1.0, pos()), pos()),
            ),
        )
}
