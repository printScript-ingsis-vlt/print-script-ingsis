package linter.rules

import ast.Assignment
import ast.NumberLiteral
import ast.Position
import ast.Program
import ast.VariableDeclaration
import linter.config.IdentifierFormat
import linter.dataclass.Severity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IdentifierFormatRuleTest {
    // --- CAMEL_CASE ---

    @Test
    fun `should accept valid camelCase variable declaration`() {
        val rule = IdentifierFormatRule(IdentifierFormat.CAMEL_CASE)
        val stmt =
            VariableDeclaration(
                name = "myVariable",
                type = "number",
                value = NumberLiteral(5.0, Position(1, 1)),
                position = Position(1, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val notifications = rule.check(program)

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `should report warning when camelCase is violated in declaration`() {
        val rule = IdentifierFormatRule(IdentifierFormat.CAMEL_CASE)
        val stmt =
            VariableDeclaration(
                // snake_case inválido para esta config
                name = "my_variable",
                type = "number",
                value = NumberLiteral(5.0, Position(1, 1)),
                position = Position(1, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val notifications = rule.check(program)

        assertEquals(1, notifications.size)
        assertEquals("identifier-format", notifications[0].rule)
        assertEquals(Severity.WARNING, notifications[0].severity)
        assertEquals(Position(1, 1), notifications[0].position)
    }

    @Test
    fun `should report warning when identifier starts with uppercase in camelCase`() {
        val rule = IdentifierFormatRule(IdentifierFormat.CAMEL_CASE)
        val stmt =
            VariableDeclaration(
                // PascalCase
                name = "MyVariable",
                type = "number",
                value = null,
                position = Position(1, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val notifications = rule.check(program)

        assertEquals(1, notifications.size)
    }

    // --- SNAKE_CASE ---

    @Test
    fun `should accept valid snake_case variable declaration`() {
        val rule = IdentifierFormatRule(IdentifierFormat.SNAKE_CASE)
        val stmt =
            VariableDeclaration(
                name = "my_variable",
                type = "number",
                value = NumberLiteral(5.0, Position(1, 1)),
                position = Position(1, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val notifications = rule.check(program)

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `should report warning when snake_case is violated in assignment`() {
        val rule = IdentifierFormatRule(IdentifierFormat.SNAKE_CASE)
        val stmt =
            Assignment(
                // camelCase inválido para esta config
                name = "myVariable",
                value = NumberLiteral(10.0, Position(2, 1)),
                position = Position(2, 1),
            )
        val program = Program(Position(1, 1), listOf(stmt))

        val notifications = rule.check(program)

        assertEquals(1, notifications.size)
        assertEquals(Position(2, 1), notifications[0].position)
    }
}
