package semantic

import ast.NumberLiteral
import ast.Program
import ast.Stmt
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import result.SemanticError

class SemanticAnalyzerConfigurationTest {
    @Test
    fun `fails when no handler supports a statement`() {
        val analyzer = SemanticAnalyzer(listOf(UnsupportedStatementHandler()))
        val program =
            Program(
                pos(),
                listOf(VariableDeclaration("count", "number", NumberLiteral(1.0, pos()), pos())),
            )

        val exception =
            assertThrows(IllegalStateException::class.java) {
                analyzer.analyze(program)
            }

        assertEquals("No semantic statement handler found for: VariableDeclaration", exception.message)
    }

    @Test
    fun `fails when multiple handlers support a statement`() {
        val analyzer = SemanticAnalyzer(listOf(AnyStatementHandler(), AnyStatementHandler()))
        val program =
            Program(
                pos(),
                listOf(VariableDeclaration("count", "number", NumberLiteral(1.0, pos()), pos())),
            )

        val exception =
            assertThrows(IllegalStateException::class.java) {
                analyzer.analyze(program)
            }

        assertEquals(
            "Ambiguous semantic statement handlers for VariableDeclaration: " +
                "AnyStatementHandler, AnyStatementHandler",
            exception.message,
        )
    }

    private class UnsupportedStatementHandler : StatementSemanticHandler {
        override fun canHandle(statement: Stmt): Boolean = false

        override fun validate(
            statement: Stmt,
            context: SemanticContext,
        ): List<SemanticError> = emptyList()

        override fun updateEnvironment(
            statement: Stmt,
            context: SemanticContext,
        ) = Unit
    }

    private class AnyStatementHandler : StatementSemanticHandler {
        override fun canHandle(statement: Stmt): Boolean = true

        override fun validate(
            statement: Stmt,
            context: SemanticContext,
        ): List<SemanticError> = emptyList()

        override fun updateEnvironment(
            statement: Stmt,
            context: SemanticContext,
        ) = Unit
    }
}
