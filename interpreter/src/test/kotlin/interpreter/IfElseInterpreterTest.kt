package interpreter

import ast.BooleanLiteral
import ast.IfStatement
import ast.NumberLiteral
import ast.Position
import ast.PrintStatement
import ast.Program
import ast.StringLiteral
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class IfElseInterpreterTest {
    private val position = Position(1, 1)

    @Test
    fun `ejecuta if con condicion true`() {
        val output = StringBuilderOutput()
        val interpreter = ConfigurableInterpreter(output)

        val program =
            Program(
                position,
                listOf(
                    IfStatement(
                        condition = BooleanLiteral(true, position),
                        thenBranch =
                            listOf(
                                PrintStatement(
                                    StringLiteral("true branch", position),
                                    position,
                                ),
                            ),
                        elseBranch = null,
                        position = position,
                    ),
                ),
            )

        interpreter.run(program)

        assertEquals("true branch", output.toString())
    }

    @Test
    fun `ejecuta else con condicion false`() {
        val output = StringBuilderOutput()
        val interpreter = ConfigurableInterpreter(output)

        val program =
            Program(
                position,
                listOf(
                    IfStatement(
                        condition = BooleanLiteral(false, position),
                        thenBranch =
                            listOf(
                                PrintStatement(
                                    StringLiteral("true branch", position),
                                    position,
                                ),
                            ),
                        elseBranch =
                            listOf(
                                PrintStatement(
                                    StringLiteral("false branch", position),
                                    position,
                                ),
                            ),
                        position = position,
                    ),
                ),
            )

        interpreter.run(program)

        assertEquals("false branch", output.toString())
    }

    @Test
    fun `variables en bloque if no afectan scope outer`() {
        val output = StringBuilderOutput()
        val interpreter = ConfigurableInterpreter(output)

        val program =
            Program(
                position,
                listOf(
                    IfStatement(
                        condition = BooleanLiteral(true, position),
                        thenBranch =
                            listOf(
                                VariableDeclaration(
                                    name = "x",
                                    type = "number",
                                    value = null,
                                    position = position,
                                ),
                            ),
                        elseBranch = null,
                        position = position,
                    ),
                    PrintStatement(
                        ast.Identifier("x", position),
                        position,
                    ),
                ),
            )

        assertThrows(IllegalStateException::class.java) {
            interpreter.run(program)
        }
    }

    @Test
    fun `no ejecuta nada cuando if es falso sin else`() {
        val output = StringBuilderOutput()
        val interpreter = ConfigurableInterpreter(output)

        val program =
            Program(
                position,
                listOf(
                    IfStatement(
                        condition = BooleanLiteral(false, position),
                        thenBranch =
                            listOf(
                                PrintStatement(StringLiteral("hola", position), position),
                            ),
                        elseBranch = null,
                        position = position,
                    ),
                ),
            )

        interpreter.run(program)

        assertEquals("", output.toString())
    }

    @Test
    fun `falla si la condicion del if no es boolean`() {
        val output = StringBuilderOutput()
        val interpreter = ConfigurableInterpreter(output)

        val program =
            Program(
                position,
                listOf(
                    IfStatement(
                        condition = NumberLiteral(5.0, position),
                        thenBranch = emptyList(),
                        elseBranch = null,
                        position = position,
                    ),
                ),
            )

        assertThrows(IllegalArgumentException::class.java) {
            interpreter.run(program)
        }
    }
}
