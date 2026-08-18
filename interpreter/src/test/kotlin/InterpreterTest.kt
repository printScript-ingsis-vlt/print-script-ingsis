import common.src.main.kotlin.result.Result
import recurses.Program
import recurses.Position
import recurses.Stmt
import common.src.main.kotlin.result.CompilerError
import common.src.main.kotlin.result.SemanticError
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import recurses.BinaryExpression
import recurses.Expr
import recurses.Identifier
import recurses.NumberLiteral
import recurses.StringLiteral
import recurses.VariableDeclaration

class InterpreterTest {

    // ---------- helpers ----------

    private fun interpret(vararg stmts: Stmt): Result<Unit, List<CompilerError>> {
        val program = Program(Position(1, 1), stmts.toList())
        return Interpreter(StringBuilderOutput()).run(program)
    }

    private fun assertSuccess(result: Result<Unit, List<CompilerError>>) {
        assertTrue("Expected Success, got $result", result is Result.Success)
    }

    private fun assertSemanticError(
        result: Result<Unit, List<CompilerError>>,
        message: String,
        position: Position
    ) {
        val failure = assertIs<Result.Failure<List<CompilerError>>>(result)
        val errors = failure.error
        assertEquals(1, errors.size)
        val error = assertIs<SemanticError>(errors.first())
        assertEquals(message, error.message)
        assertEquals(position, error.position)
    }

    private fun decl(name: String, type: String, value: Expr? = null, pos: Position = Position(1, 1)) =
        VariableDeclaration(name, type, value, pos)

    @Test
    fun `declaracion de number con inicializador`() {
        assertSuccess(interpret(decl("x", "number", NumberLiteral(12.0, Position(1, 15)))))
    }

    @Test
    fun `declaracion de string con inicializador`() {
        assertSuccess(interpret(decl("name", "string", StringLiteral("Joe", Position(1, 16)))))
    }

    @Test
    fun `declaracion sin inicializador`() {
        assertSuccess(interpret(decl("x", "number")))
    }

    @Test
    fun `multiples declaraciones consecutivas`() {
        assertSuccess(
            interpret(
                decl("a", "number", NumberLiteral(12.0, Position(1, 15))),
                decl("b", "number", NumberLiteral(4.0, Position(2, 15))),
                decl("c", "number", BinaryExpression(
                    Identifier("a", Position(3, 15)),
                    "/",
                    Identifier("b", Position(3, 19)),
                    Position(3, 15)
                )
                )
            )
        )
    }

    @Test
    fun `suma aritmetica`() {
        assertSuccess(interpret(decl("x", "number", BinaryExpression(
            NumberLiteral(2.0, Position(1, 15)), "+", NumberLiteral(3.0, Position(1, 19)), Position(1, 15)
        ))))
    }

    @Test
    fun `resta aritmetica`() {
        assertSuccess(interpret(decl("x", "number", BinaryExpression(
            NumberLiteral(5.0, Position(1, 15)), "-", NumberLiteral(2.0, Position(1, 19)), Position(1, 15)
        ))))
    }

    @Test
    fun `multiplicacion aritmetica`() {
        assertSuccess(interpret(decl("x", "number", BinaryExpression(
            NumberLiteral(6.0, Position(1, 15)), "*", NumberLiteral(7.0, Position(1, 19)), Position(1, 15)
        ))))
    }

    @Test
    fun `division aritmetica`() {
        assertSuccess(interpret(decl("c", "number", BinaryExpression(
            NumberLiteral(12.0, Position(1, 15)), "/", NumberLiteral(4.0, Position(1, 20)), Position(1, 15)
        ))))
    }

    @Test
    fun `numero decimal`() {
        assertSuccess(interpret(decl("x", "number", NumberLiteral(12.5, Position(1, 15)))))
    }

    @Test
    fun `concatenacion de strings`() {
        assertSuccess(interpret(decl("greeting", "string", BinaryExpression(
            StringLiteral("Hello", Position(1, 18)),
            "+",
            StringLiteral(" World", Position(1, 26)),
            Position(1, 18)
        ))))
    }

    @Test
    fun `concatenacion de string y number`() {
        assertSuccess(interpret(decl("result", "string", BinaryExpression(
            StringLiteral("Result: ", Position(1, 18)),
            "+",
            NumberLiteral(3.0, Position(1, 29)),
            Position(1, 18)
        ))))
    }

    // ---------- casos de error ----------

    @Test
    fun `tipo invalido`() {
        assertSemanticError(
            interpret(decl("x", "boolean")),
            "Invalid type 'boolean'",
            Position(1, 1)
        )
    }

    @Test
    fun `tipo incompatible en asignacion inicial`() {
        assertSemanticError(
            interpret(decl("x", "number", StringLiteral("abc", Position(1, 15)))),
            "Cannot assign a value of type 'string' to a variable of type 'number'",
            Position(1, 1)
        )
    }

    @Test
    fun `variable no declarada`() {
        assertSemanticError(
            interpret(decl("x", "number", Identifier("y", Position(1, 15)))),
            "Variable 'y' is not declared",
            Position(1, 15)
        )
    }

    @Test
    fun `variable no inicializada`() {
        assertSemanticError(
            interpret(
                decl("x", "number"),
                decl("y", "number", Identifier("x", Position(2, 15)), Position(2, 1))
            ),
            "Variable 'x' is not initialized",
            Position(2, 15)
        )
    }

    @Test
    fun `operador desconocido`() {
        assertSemanticError(
            interpret(decl("x", "number", BinaryExpression(
                NumberLiteral(1.0, Position(1, 15)), "%", NumberLiteral(2.0, Position(1, 19)), Position(1, 15)
            ))),
            "Unknown operator '%'",
            Position(1, 15)
        )
    }

    @Test
    fun `operador aritmetico con operandos string`() {
        assertSemanticError(
            interpret(decl("x", "number", BinaryExpression(
                StringLiteral("a", Position(1, 15)), "-", StringLiteral("b", Position(1, 19)), Position(1, 15)
            ))),
            "Operator '-' requires numeric operands",
            Position(1, 15)
        )
    }

    @Test
    fun `division por cero`() {
        assertSemanticError(
            interpret(decl("x", "number", BinaryExpression(
                NumberLiteral(1.0, Position(1, 15)), "/", NumberLiteral(0.0, Position(1, 19)), Position(1, 15)
            ))),
            "Division by zero",
            Position(1, 15)
        )
    }

    // ---------- formato de valores (salida) ----------

    @Test
    fun `number entero se imprime sin decimales`() {
        assertEquals("5", NumberValue(5.0).asString())
    }

    @Test
    fun `number decimal conserva su parte decimal`() {
        assertEquals("12.5", NumberValue(12.5).asString())
    }

    @Test
    fun `number de division entera`() {
        assertEquals("3", NumberValue(12.0 / 4.0).asString())
    }

    @Test
    fun `string conserva su valor`() {
        assertEquals("Joe", StringValue("Joe").asString())
    }

    @Test
    fun `concatena number como string`() {
        assertEquals("Result: 3", "Result: " + NumberValue(3.0).asString())
    }
}
