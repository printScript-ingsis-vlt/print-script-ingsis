package parser.streamRules

import ast.Assignment
import ast.BinaryExpression
import ast.BooleanLiteral
import ast.IfStatement
import ast.NumberLiteral
import ast.PrintScriptVersion
import ast.PrintStatement
import ast.ReadEnvExpression
import ast.ReadInputExpression
import ast.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.tok
import result.Result
import token.TokenType

class StreamGrammarRulesTest {
    @Test
    fun `test v1_0 parses let declaration, assignment and println`() {
        val parser = StreamGrammarConfigurations.getConfiguration(PrintScriptVersion.V1_0)
        val tokens =
            listOf(
                tok(TokenType.LET),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.COLON),
                tok(TokenType.IDENTIFIER, "number"),
                tok(TokenType.EQUAL),
                tok(TokenType.NUMBER_LITERAL, "42"),
                tok(TokenType.SEMICOLON),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.EQUAL),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.PLUS, "+"),
                tok(TokenType.NUMBER_LITERAL, "1"),
                tok(TokenType.SEMICOLON),
                tok(TokenType.IDENTIFIER, "println"),
                tok(TokenType.LEFT_PAREN),
                tok(TokenType.IDENTIFIER, "x"),
                tok(TokenType.RIGHT_PAREN),
                tok(TokenType.SEMICOLON),
                tok(TokenType.EOF),
            )

        val result = parser.parse(tokens)
        assertTrue(result is Result.Success)
        val program = (result as Result.Success).value
        assertEquals(3, program.statements.size)

        val decl = program.statements[0] as VariableDeclaration
        assertEquals("x", decl.name)
        assertEquals("number", decl.type)
        assertTrue(decl.mutable)
        assertEquals(42.0, (decl.value as NumberLiteral).value)

        val assign = program.statements[1] as Assignment
        assertEquals("x", assign.name)
        assertTrue(assign.value is BinaryExpression)

        val print = program.statements[2] as PrintStatement
        assertNotNull(print.argument)
    }

    // Es un test no veo la utilidad de hacer una extracción para algo que no contiene logica y no va a cambiar y
    // solo es comportamiento
    @Test
    @Suppress("LongMethod")
    fun `test v1_1 parses const, boolean, readInput, readEnv and if else`() {
        val parser = StreamGrammarConfigurations.getConfiguration(PrintScriptVersion.V1_1)
        val tokens =
            listOf(
                // const b: boolean = true;
                tok(TokenType.CONST),
                tok(TokenType.IDENTIFIER, "b"),
                tok(TokenType.COLON),
                tok(TokenType.BOOLEAN),
                tok(TokenType.EQUAL),
                tok(TokenType.TRUE),
                tok(TokenType.SEMICOLON),
                // let env: string = readEnv("PATH");
                tok(TokenType.LET),
                tok(TokenType.IDENTIFIER, "env"),
                tok(TokenType.COLON),
                tok(TokenType.IDENTIFIER, "string"),
                tok(TokenType.EQUAL),
                tok(TokenType.READENV),
                tok(TokenType.LEFT_PAREN),
                tok(TokenType.STRING_LITERAL, "PATH"),
                tok(TokenType.RIGHT_PAREN),
                tok(TokenType.SEMICOLON),
                // let input: string = readInput("Enter: ");
                tok(TokenType.LET),
                tok(TokenType.IDENTIFIER, "input"),
                tok(TokenType.COLON),
                tok(TokenType.IDENTIFIER, "string"),
                tok(TokenType.EQUAL),
                tok(TokenType.READINPUT),
                tok(TokenType.LEFT_PAREN),
                tok(TokenType.STRING_LITERAL, "Enter: "),
                tok(TokenType.RIGHT_PAREN),
                tok(TokenType.SEMICOLON),
                // if (b) { println(b); } else { println("false"); }
                tok(TokenType.IF),
                tok(TokenType.LEFT_PAREN),
                tok(TokenType.IDENTIFIER, "b"),
                tok(TokenType.RIGHT_PAREN),
                tok(TokenType.LEFT_BRACE),
                tok(TokenType.IDENTIFIER, "println"),
                tok(TokenType.LEFT_PAREN),
                tok(TokenType.IDENTIFIER, "b"),
                tok(TokenType.RIGHT_PAREN),
                tok(TokenType.SEMICOLON),
                tok(TokenType.RIGHT_BRACE),
                tok(TokenType.ELSE),
                tok(TokenType.LEFT_BRACE),
                tok(TokenType.IDENTIFIER, "println"),
                tok(TokenType.LEFT_PAREN),
                tok(TokenType.STRING_LITERAL, "false"),
                tok(TokenType.RIGHT_PAREN),
                tok(TokenType.SEMICOLON),
                tok(TokenType.RIGHT_BRACE),
                tok(TokenType.EOF),
            )

        val result = parser.parse(tokens)
        assertTrue(result is Result.Success)
        val program = (result as Result.Success).value
        assertEquals(4, program.statements.size)

        val constDecl = program.statements[0] as VariableDeclaration
        assertEquals("b", constDecl.name)
        assertFalse(constDecl.mutable)
        val boolLit = constDecl.value as BooleanLiteral
        assertTrue(boolLit.value)

        val envDecl = program.statements[1] as VariableDeclaration
        assertTrue(envDecl.value is ReadEnvExpression)

        val inputDecl = program.statements[2] as VariableDeclaration
        assertTrue(inputDecl.value is ReadInputExpression)

        val ifStmt = program.statements[3] as IfStatement
        assertEquals(1, ifStmt.thenBranch.size)
        assertNotNull(ifStmt.elseBranch)
        assertEquals(1, ifStmt.elseBranch!!.size)
    }
}
