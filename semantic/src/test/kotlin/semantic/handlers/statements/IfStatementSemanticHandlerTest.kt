package semantic.handlers.statements

import ast.Assignment
import ast.BooleanLiteral
import ast.Identifier
import ast.IfStatement
import ast.NumberLiteral
import ast.PrintStatement
import ast.Program
import ast.StringLiteral
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import semantic.SemanticAnalyzer
import semantic.SemanticConfigurations
import semantic.pos

class IfStatementSemanticHandlerTest {
    private val analyzer = SemanticAnalyzer(SemanticConfigurations.v1_1)

    @Test
    fun `accepts a boolean variable as an if condition`() {
        val program =
            Program(
                pos(),
                listOf(
                    VariableDeclaration("enabled", "boolean", BooleanLiteral(true, pos()), pos()),
                    IfStatement(Identifier("enabled", pos()), emptyList(), null, pos()),
                ),
            )

        assertTrue(analyzer.analyze(program).isEmpty())
    }

    @Test
    fun `rejects a non boolean variable as an if condition`() {
        val program =
            Program(
                pos(),
                listOf(
                    VariableDeclaration("count", "number", NumberLiteral(1.0, pos()), pos()),
                    IfStatement(Identifier("count", pos()), emptyList(), null, pos()),
                ),
            )

        val errors = analyzer.analyze(program)

        assertEquals("If condition variable 'count' must be boolean", errors.single().message)
    }

    @Test
    fun `rejects a literal as an if condition`() {
        val program = Program(pos(), listOf(IfStatement(BooleanLiteral(true, pos()), emptyList(), null, pos())))

        val errors = analyzer.analyze(program)

        assertEquals("If condition must be a boolean variable", errors.single().message)
    }

    @Test
    fun `does not expose variables declared inside a branch`() {
        val program =
            Program(
                pos(),
                listOf(
                    VariableDeclaration("enabled", "boolean", BooleanLiteral(true, pos()), pos()),
                    IfStatement(
                        Identifier("enabled", pos()),
                        listOf(VariableDeclaration("local", "string", StringLiteral("text", pos()), pos())),
                        null,
                        pos(),
                    ),
                    PrintStatement(Identifier("local", pos()), pos()),
                ),
            )

        val errors = analyzer.analyze(program)

        assertEquals("Variable 'local' is not declared", errors.single().message)
    }

    @Test
    fun `marks an outer variable initialized when both branches assign it`() {
        val program =
            Program(
                pos(),
                listOf(
                    VariableDeclaration("enabled", "boolean", BooleanLiteral(true, pos()), pos()),
                    VariableDeclaration("message", "string", null, pos()),
                    IfStatement(
                        Identifier("enabled", pos()),
                        listOf(Assignment("message", StringLiteral("on", pos()), pos())),
                        listOf(Assignment("message", StringLiteral("off", pos()), pos())),
                        pos(),
                    ),
                    PrintStatement(Identifier("message", pos()), pos()),
                ),
            )

        assertTrue(analyzer.analyze(program).isEmpty())
    }

    @Test
    fun `does not mark an outer variable initialized when only one branch assigns it`() {
        val program =
            Program(
                pos(),
                listOf(
                    VariableDeclaration("enabled", "boolean", BooleanLiteral(true, pos()), pos()),
                    VariableDeclaration("message", "string", null, pos()),
                    IfStatement(
                        Identifier("enabled", pos()),
                        listOf(Assignment("message", StringLiteral("on", pos()), pos())),
                        null,
                        pos(),
                    ),
                    PrintStatement(Identifier("message", pos()), pos()),
                ),
            )

        val errors = analyzer.analyze(program)

        assertEquals("Variable 'message' is not initialized", errors.single().message)
    }
}
