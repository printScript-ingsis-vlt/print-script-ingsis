package semantic.handlers.statements

import ast.Assignment
import ast.Identifier
import ast.NumberLiteral
import ast.PrintStatement
import ast.StringLiteral
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import semantic.SemanticContext
import semantic.expressions.ExpressionSemanticAnalyzer
import semantic.handlers.expressions.BinaryExpressionSemanticHandler
import semantic.handlers.expressions.IdentifierSemanticHandler
import semantic.handlers.expressions.NumberLiteralSemanticHandler
import semantic.handlers.expressions.StringLiteralSemanticHandler
import semantic.pos
import semantic.symbols.SemanticSymbol

class StatementSemanticHandlersTest {
    @Test
    fun `valid declaration is validated and added to the symbol table`() {
        val context = SemanticContext()
        val handler = VariableDeclarationSemanticHandler(expressionAnalyzer())
        val statement = VariableDeclaration("count", "number", NumberLiteral(1.0, pos()), pos())

        assertTrue(handler.validate(statement, context).isEmpty())

        handler.updateEnvironment(statement, context)

        assertEquals("number", context.symbols.lookup("count")?.type)
        assertEquals(1.0, context.symbols.lookup("count")?.knownNumberValue)
    }

    @Test
    fun `invalid declarations report their type errors without updating the symbol table`() {
        val context = SemanticContext()
        val handler = VariableDeclarationSemanticHandler(expressionAnalyzer())
        val statement = VariableDeclaration("count", "number", StringLiteral("text", pos()), pos())

        val errors = handler.validate(statement, context)

        assertEquals("Cannot assign string to number", errors.single().message)
        assertNull(context.symbols.lookup("count"))
    }

    @Test
    fun `assignments validate the target and update its semantic value`() {
        val context = SemanticContext()
        context.symbols.declare("count", SemanticSymbol("number", initialized = true, knownNumberValue = 1.0))
        val handler = AssignmentSemanticHandler(expressionAnalyzer())
        val statement = Assignment("count", NumberLiteral(2.0, pos()), pos())

        assertTrue(handler.validate(statement, context).isEmpty())

        handler.updateEnvironment(statement, context)

        assertEquals(2.0, context.symbols.lookup("count")?.knownNumberValue)
    }

    @Test
    fun `assignments report missing targets and incompatible types`() {
        val context = SemanticContext()
        context.symbols.declare("count", SemanticSymbol("number", initialized = true, knownNumberValue = 1.0))
        val handler = AssignmentSemanticHandler(expressionAnalyzer())

        val missingErrors = handler.validate(Assignment("missing", NumberLiteral(1.0, pos()), pos()), context)
        val incompatibleErrors = handler.validate(Assignment("count", StringLiteral("text", pos()), pos()), context)

        assertEquals("Variable 'missing' is not declared", missingErrors.single().message)
        assertEquals("Cannot assign string to number", incompatibleErrors.single().message)
    }

    @Test
    fun `print validates its argument without modifying the symbol table`() {
        val context = SemanticContext()
        val handler = PrintStatementSemanticHandler(expressionAnalyzer())
        val statement = PrintStatement(Identifier("missing", pos()), pos())

        val errors = handler.validate(statement, context)

        assertEquals("Variable 'missing' is not declared", errors.single().message)
        handler.updateEnvironment(statement, context)
        assertNull(context.symbols.lookup("missing"))
    }

    private fun expressionAnalyzer(): ExpressionSemanticAnalyzer =
        ExpressionSemanticAnalyzer(
            listOf(
                NumberLiteralSemanticHandler(),
                StringLiteralSemanticHandler(),
                IdentifierSemanticHandler(),
                BinaryExpressionSemanticHandler(),
            ),
        )
}
