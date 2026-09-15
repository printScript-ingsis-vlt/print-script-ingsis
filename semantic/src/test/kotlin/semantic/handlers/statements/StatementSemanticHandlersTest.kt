package semantic.handlers.statements

import ast.Assignment
import ast.BooleanLiteral
import ast.Expr
import ast.Identifier
import ast.NumberLiteral
import ast.PrintStatement
import ast.StringLiteral
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import result.SemanticError
import semantic.SemanticContext
import semantic.StatementSemanticHandler
import semantic.StatementSemanticTraversal
import semantic.expressions.ExpressionSemanticAnalyzer
import semantic.handlers.expressions.BinaryExpressionSemanticHandler
import semantic.handlers.expressions.BooleanLiteralSemanticHandler
import semantic.handlers.expressions.IdentifierSemanticHandler
import semantic.handlers.expressions.NumberLiteralSemanticHandler
import semantic.handlers.expressions.StringLiteralSemanticHandler
import semantic.pos
import semantic.symbols.SemanticSymbol

class StatementSemanticHandlersTest {
    @Test
    fun `valid declaration is validated and added to the symbol table`() {
        val context = SemanticContext()
        val handler = variableDeclarationHandler()
        val statement = VariableDeclaration("count", "number", NumberLiteral(1.0, pos()), pos())

        assertTrue(handler.validate(statement, context, expressionAnalysis(context)).isEmpty())

        handler.updateEnvironment(statement, context, expressionAnalysis(context))

        assertEquals("number", context.symbols.lookup("count")?.type)
        assertEquals(1.0, context.symbols.lookup("count")?.knownNumberValue)
    }

    @Test
    fun `constant declaration is added as immutable to the symbol table`() {
        val context = SemanticContext()
        val handler = variableDeclarationHandler()
        val statement =
            VariableDeclaration(
                name = "limit",
                type = "number",
                value = NumberLiteral(10.0, pos()),
                position = pos(),
                mutable = false,
            )

        assertTrue(handler.validate(statement, context, expressionAnalysis(context)).isEmpty())
        handler.updateEnvironment(statement, context, expressionAnalysis(context))

        assertEquals(false, context.symbols.lookup("limit")?.mutable)
    }

    @Test
    fun `invalid declarations report their type errors without updating the symbol table`() {
        val context = SemanticContext()
        val handler = variableDeclarationHandler()
        val statement = VariableDeclaration("count", "number", StringLiteral("text", pos()), pos())

        val errors = handler.validate(statement, context, expressionAnalysis(context))

        assertEquals("Cannot assign string to number", errors.single().message)
        assertNull(context.symbols.lookup("count"))
    }

    @Test
    fun `assignments validate the target and update its semantic value`() {
        val context = SemanticContext()
        context.symbols.declare("count", SemanticSymbol("number", initialized = true, knownNumberValue = 1.0))
        val handler = AssignmentSemanticHandler()
        val statement = Assignment("count", NumberLiteral(2.0, pos()), pos())

        assertTrue(handler.validate(statement, context, expressionAnalysis(context)).isEmpty())

        handler.updateEnvironment(statement, context, expressionAnalysis(context))

        assertEquals(2.0, context.symbols.lookup("count")?.knownNumberValue)
    }

    @Test
    fun `assignments report missing targets and incompatible types`() {
        val context = SemanticContext()
        context.symbols.declare("count", SemanticSymbol("number", initialized = true, knownNumberValue = 1.0))
        val handler = AssignmentSemanticHandler()

        val missingErrors =
            handler.validate(
                Assignment("missing", NumberLiteral(1.0, pos()), pos()),
                context,
                expressionAnalysis(context),
            )
        val incompatibleErrors =
            handler.validate(
                Assignment("count", StringLiteral("text", pos()), pos()),
                context,
                expressionAnalysis(context),
            )

        assertEquals("Variable 'missing' is not declared", missingErrors.single().message)
        assertEquals("Cannot assign string to number", incompatibleErrors.single().message)
    }

    @Test
    fun `assignments to constants are rejected`() {
        val context = SemanticContext()
        context.symbols.declare(
            "limit",
            SemanticSymbol("number", initialized = true, mutable = false, knownNumberValue = 10.0),
        )
        val handler = AssignmentSemanticHandler()

        val errors =
            handler.validate(
                Assignment("limit", NumberLiteral(20.0, pos()), pos()),
                context,
                expressionAnalysis(context),
            )

        assertEquals("Cannot reassign constant 'limit'", errors.single().message)
    }

    @Test
    fun `print validates its argument without modifying the symbol table`() {
        val context = SemanticContext()
        val handler = PrintStatementSemanticHandler()
        val statement = PrintStatement(Identifier("missing", pos()), pos())

        val errors = handler.validate(statement, context, expressionAnalysis(context))

        assertEquals("Variable 'missing' is not declared", errors.single().message)
        handler.updateEnvironment(statement, context, expressionAnalysis(context))
        assertNull(context.symbols.lookup("missing"))
    }

    @Test
    fun `boolean declarations and assignments validate compatible values`() {
        val context = SemanticContext()
        val declarationHandler = variableDeclarationHandler()
        val declaration = VariableDeclaration("enabled", "boolean", BooleanLiteral(true, pos()), pos())

        assertTrue(declarationHandler.validate(declaration, context, expressionAnalysis(context)).isEmpty())
        declarationHandler.updateEnvironment(declaration, context, expressionAnalysis(context))

        val assignmentHandler = AssignmentSemanticHandler()
        val assignment = Assignment("enabled", BooleanLiteral(false, pos()), pos())
        assertTrue(assignmentHandler.validate(assignment, context, expressionAnalysis(context)).isEmpty())
        assignmentHandler.updateEnvironment(assignment, context, expressionAnalysis(context))

        assertEquals("boolean", context.symbols.lookup("enabled")?.type)
        assertTrue(context.symbols.lookup("enabled")?.initialized == true)
    }

    @Test
    fun `boolean variables reject values of another type`() {
        val context = SemanticContext()
        val handler = variableDeclarationHandler()
        val statement = VariableDeclaration("enabled", "boolean", NumberLiteral(1.0, pos()), pos())

        val errors = handler.validate(statement, context, expressionAnalysis(context))

        assertEquals("Cannot assign number to boolean", errors.single().message)
    }

    private fun expressionAnalyzer(): ExpressionSemanticAnalyzer =
        ExpressionSemanticAnalyzer(
            listOf(
                NumberLiteralSemanticHandler(),
                StringLiteralSemanticHandler(),
                BooleanLiteralSemanticHandler(),
                IdentifierSemanticHandler(),
                BinaryExpressionSemanticHandler(),
            ),
        )

    private fun variableDeclarationHandler(): VariableDeclarationSemanticHandler =
        VariableDeclarationSemanticHandler(
            supportedTypes = setOf("number", "string", "boolean"),
            constantsAllowed = true,
        )

    private fun expressionAnalysis(context: SemanticContext): (Expr) -> semantic.expressions.ExpressionAnalysis {
        val analyzer = expressionAnalyzer()
        return { expression -> analyzer.analyze(expression, context) }
    }

    private fun StatementSemanticHandler.validate(
        statement: ast.Stmt,
        context: SemanticContext,
        analyzeExpression: (Expr) -> semantic.expressions.ExpressionAnalysis,
    ): List<SemanticError> = validate(statement, context, analyzeExpression, NoOpStatementSemanticTraversal)

    private fun StatementSemanticHandler.updateEnvironment(
        statement: ast.Stmt,
        context: SemanticContext,
        analyzeExpression: (Expr) -> semantic.expressions.ExpressionAnalysis,
    ) {
        updateEnvironment(statement, context, analyzeExpression, NoOpStatementSemanticTraversal)
    }

    private object NoOpStatementSemanticTraversal : StatementSemanticTraversal {
        override fun validateAndUpdate(
            statements: List<ast.Stmt>,
            context: SemanticContext,
        ): List<SemanticError> = emptyList()

        override fun update(
            statements: List<ast.Stmt>,
            context: SemanticContext,
        ) = Unit
    }
}
