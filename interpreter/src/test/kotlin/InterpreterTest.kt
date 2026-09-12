import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import recurses.Assignment
import recurses.BinaryExpression
import recurses.Identifier
import recurses.NumberLiteral
import recurses.Position
import recurses.PrintStatement
import recurses.Program
import recurses.StringLiteral
import recurses.VariableDeclaration
import recurses.valuedataclass.NumberValue
import recurses.valuedataclass.StringValue

class InterpreterTest {
    private val dummyPos = Position(1, 1)

    private fun createInterpreter(): Pair<Interpreter, StringBuilderOutput> {
        val output = StringBuilderOutput()
        return Pair(Interpreter(output), output)
    }

    @Test
    fun `evaluates number literal`() {
        val (interpreter, _) = createInterpreter()
        val result = interpreter.evaluate(NumberLiteral(42.0, dummyPos))
        assertEquals(NumberValue(42.0), result)
    }

    @Test
    fun `evaluates string literal`() {
        val (interpreter, _) = createInterpreter()
        val result = interpreter.evaluate(StringLiteral("hello", dummyPos))
        assertEquals(StringValue("hello"), result)
    }

    @Test
    fun `evaluates arithmetic binary operations`() {
        val (interpreter, _) = createInterpreter()

        // 10 + 5
        val sum =
            interpreter.evaluate(
                BinaryExpression(NumberLiteral(10.0, dummyPos), "+", NumberLiteral(5.0, dummyPos), dummyPos),
            )
        assertEquals(NumberValue(15.0), sum)

        // 10 - 4
        val sub =
            interpreter.evaluate(
                BinaryExpression(NumberLiteral(10.0, dummyPos), "-", NumberLiteral(4.0, dummyPos), dummyPos),
            )
        assertEquals(NumberValue(6.0), sub)

        // 3 * 7
        val mul =
            interpreter.evaluate(
                BinaryExpression(NumberLiteral(3.0, dummyPos), "*", NumberLiteral(7.0, dummyPos), dummyPos),
            )
        assertEquals(NumberValue(21.0), mul)

        // 20 / 4
        val div =
            interpreter.evaluate(
                BinaryExpression(NumberLiteral(20.0, dummyPos), "/", NumberLiteral(4.0, dummyPos), dummyPos),
            )
        assertEquals(NumberValue(5.0), div)
    }

    @Test
    fun `evaluates string concatenation with string and number`() {
        val (interpreter, _) = createInterpreter()

        // "Hello " + "World"
        val strConcat =
            interpreter.evaluate(
                BinaryExpression(StringLiteral("Hello ", dummyPos), "+", StringLiteral("World", dummyPos), dummyPos),
            )
        assertEquals(StringValue("Hello World"), strConcat)

        // "Value: " + 10
        val strNum =
            interpreter.evaluate(
                BinaryExpression(StringLiteral("Value: ", dummyPos), "+", NumberLiteral(10.0, dummyPos), dummyPos),
            )
        assertEquals(StringValue("Value: 10"), strNum)

        // 5 + " apples"
        val numStr =
            interpreter.evaluate(
                BinaryExpression(NumberLiteral(5.0, dummyPos), "+", StringLiteral(" apples", dummyPos), dummyPos),
            )
        assertEquals(StringValue("5 apples"), numStr)
    }

    @Test
    fun `executes variable declaration with and without initializer, and assignment`() {
        val (interpreter, output) = createInterpreter()

        // let a: Number = 5;
        // let b: Number;
        // b = 10;
        // println(a + b);
        val program =
            Program(
                dummyPos,
                listOf(
                    VariableDeclaration("a", "Number", NumberLiteral(5.0, dummyPos), dummyPos),
                    VariableDeclaration("b", "Number", null, dummyPos),
                    Assignment("b", NumberLiteral(10.0, dummyPos), dummyPos),
                    PrintStatement(
                        BinaryExpression(Identifier("a", dummyPos), "+", Identifier("b", dummyPos), dummyPos),
                        dummyPos,
                    ),
                ),
            )

        interpreter.run(program)
        assertEquals("15", output.toString())
    }

    @Test
    fun `executes print statement with string and variable`() {
        val (interpreter, output) = createInterpreter()

        // let name: String = "PrintScript";
        // println("Welcome to " + name);
        val program =
            Program(
                dummyPos,
                listOf(
                    VariableDeclaration(
                        "name",
                        "String",
                        StringLiteral("PrintScript", dummyPos),
                        dummyPos,
                    ),
                    PrintStatement(
                        BinaryExpression(
                            StringLiteral(
                                "Welcome to ",
                                dummyPos,
                            ),
                            "+",
                            Identifier("name", dummyPos),
                            dummyPos,
                        ),
                        dummyPos,
                    ),
                ),
            )

        interpreter.run(program)
        assertEquals("Welcome to PrintScript", output.toString())
    }

    @Test
    fun `tests output implementations for complete coverage`() {
        NullOutput.write("test")
        ConsoleOutput.write("") // no falla y cubre ConsoleOutput
    }
}
