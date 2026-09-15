package cli.commands

import com.github.ajalt.clikt.testing.test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class InterpretCommandTest {
    @Test
    fun `debe ejecutar con version por defecto 1_1`(
        @TempDir tempDir: File,
    ) {
        val script = File(tempDir, "script.ps").apply { writeText("println(1);") }
        val command = InterpretCommand()

        val result = command.test("${script.absolutePath}")

        assertEquals(0, result.statusCode)
    }

    @Test
    fun `debe aceptar flag --version 1_0`(
        @TempDir tempDir: File,
    ) {
        val script = File(tempDir, "script.ps").apply { writeText("println(1);") }
        val command = InterpretCommand()

        val result = command.test("--version 1.0 ${script.absolutePath}")

        assertEquals(0, result.statusCode)
    }

    @Test
    fun `debe aceptar flag --lang-version 1_1`(
        @TempDir tempDir: File,
    ) {
        val script = File(tempDir, "script.ps").apply { writeText("println(1);") }
        val command = InterpretCommand()

        val result = command.test("--lang-version 1.1 ${script.absolutePath}")

        assertEquals(0, result.statusCode)
    }
}
