package semantic

import ast.Identifier
import ast.NumberLiteral
import ast.PrintStatement
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StatementSemanticTraversalTest {
    private val analyzer = SemanticAnalyzer(SemanticConfigurations.v1_0)

    @Test
    fun `validate and update advances the received context between statements`() {
        val context = SemanticContext()
        val statements =
            listOf(
                VariableDeclaration("count", "number", NumberLiteral(1.0, pos()), pos()),
                PrintStatement(Identifier("count", pos()), pos()),
            )

        val errors = analyzer.validateAndUpdate(statements, context)

        assertTrue(errors.isEmpty())
        assertEquals("number", context.symbols.lookup("count")?.type)
    }

    @Test
    fun `update applies statements that were already validated`() {
        val context = SemanticContext()
        val statement = VariableDeclaration("count", "number", NumberLiteral(1.0, pos()), pos())

        analyzer.update(listOf(statement), context)

        assertEquals(1.0, context.symbols.lookup("count")?.knownNumberValue)
    }
}
