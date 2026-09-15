package cli

import cli.commands.InterpretCommand
import com.github.ajalt.clikt.testing.test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CliVersionIntegrationTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `ejecucion con version 1_0 falla al procesar features de 1_1`() {
        // Creamos un script con sintaxis 1.1 (if/else)
        val script =
            File(tempDir, "script_v11.ps").apply {
                writeText("if (true) { println(\"hola\"); }")
            }

        val cmd = InterpretCommand()
        val result = cmd.test("--version 1.0 ${script.absolutePath}")

        // Debe fallar en parsing o semántica al no reconocer los tokens/bloques en 1.0
        assertTrue(result.stdout.contains("Error") || result.stderr.contains("Error"))
    }

    @Test
    fun `ejecucion por defecto (1_1) procesa sintaxis v1_1 correctamente`() {
        val script =
            File(tempDir, "script_v11.ps").apply {
                writeText("let a: boolean = true;")
            }

        val cmd = InterpretCommand()
        val result = cmd.test(script.absolutePath) // Sin flag, usa default 1.1

        assertEquals(0, result.statusCode)
    }
}
