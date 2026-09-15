package cli.commands

import com.github.ajalt.clikt.testing.test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CliCommandsTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var validV10File: File
    private lateinit var validV11File: File
    private lateinit var lexErrorFile: File
    private lateinit var syntaxErrorFile: File
    private lateinit var semanticErrorFile: File

    @BeforeEach
    fun setUp() {
        validV10File =
            File(tempDir, "valid_v10.ps").apply {
                writeText("let x: number = 5;\nprintln(x);")
            }

        validV11File =
            File(tempDir, "valid_v11.ps").apply {
                writeText("const y: boolean = true;\nif (y) {\n println(1);\n}")
            }

        lexErrorFile =
            File(tempDir, "lex_error.ps").apply {
                writeText("let a: number = @#$;")
            }

        syntaxErrorFile =
            File(tempDir, "syntax_error.ps").apply {
                writeText("let : number = 5;")
            }

        semanticErrorFile =
            File(tempDir, "semantic_error.ps").apply {
                writeText("let x: number = \"texto\";")
            }
    }

    // --- MyLangCli ---

    @Test
    fun `MyLangCli ejecuta el comando base correctamente`() {
        val result = MyLangCli().test()
        assertEquals(0, result.statusCode)
    }

    // --- LexCommand ---

    @Test
    fun `LexCommand tokeniza exitosamente con version por defecto`() {
        val result = LexCommand().test(validV10File.absolutePath)
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `LexCommand tokeniza con flag de version explicito`() {
        val result = LexCommand().test("--version 1.0 ${validV10File.absolutePath}")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `LexCommand muestra error cuando falla el lexer`() {
        val result = LexCommand().test(lexErrorFile.absolutePath)
        assertTrue(result.stderr.contains("Error léxico") || result.stdout.contains("Error léxico"))
    }

    // --- InterpretCommand ---

    @Test
    fun `InterpretCommand ejecuta script valido en version 1_0`() {
        val result = InterpretCommand().test("--version 1.0 ${validV10File.absolutePath}")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `InterpretCommand ejecuta script valido en version 1_1 por defecto`() {
        val result = InterpretCommand().test(validV11File.absolutePath)
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `InterpretCommand aborta si la carga del programa falla`() {
        val result = InterpretCommand().test(syntaxErrorFile.absolutePath)
        assertTrue(result.stderr.contains("Error de sintaxis") || result.stdout.contains("Error de sintaxis"))
    }

    // --- FormatCommand ---

    @Test
    fun `FormatCommand imprime formateo en STDOUT sin flag write`() {
        val result = FormatCommand().test(validV10File.absolutePath)
        assertEquals(0, result.statusCode)
        assertTrue(result.stdout.isNotEmpty())
    }

    @Test
    fun `FormatCommand reescribe archivo con flag -w`() {
        val result = FormatCommand().test("-w ${validV10File.absolutePath}")
        assertEquals(0, result.statusCode)
        assertTrue(result.stdout.contains("Archivo formateado"))
    }

    @Test
    fun `FormatCommand aborta si el programa no carga`() {
        val result = FormatCommand().test(lexErrorFile.absolutePath)
        assertTrue(result.stderr.contains("Error léxico") || result.stdout.contains("Error léxico"))
    }

    // --- LintCommand ---

    @Test
    fun `LintCommand informa script limpio`() {
        val result = LintCommand().test(validV10File.absolutePath)
        assertEquals(0, result.statusCode)
        assertTrue(result.stdout.contains("No se encontraron problemas."))
    }

    @Test
    fun `LintCommand aborta si el programa falla en etapa previa`() {
        val result = LintCommand().test(syntaxErrorFile.absolutePath)
        assertTrue(result.stderr.contains("Error de sintaxis") || result.stdout.contains("Error de sintaxis"))
    }

    // --- loadProgram & validateSemantics coverage ---

    @Test
    fun `loadProgram captura errores semanticos`() {
        val result = InterpretCommand().test(semanticErrorFile.absolutePath)
        assertTrue(result.stderr.contains("Error semántico") || result.stdout.contains("Error semántico"))
    }
}
