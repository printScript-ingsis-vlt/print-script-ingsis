package pipeline

import ast.PrintScriptVersion
import interpreter.MockEnvProvider
import interpreter.StringBuilderOutput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.StringReader

class StreamPipelineTest {
    @Test
    fun `test pipeline runs simple v1_0 program successfully`() {
        val code =
            """
            let x: number = 10;
            let y: number = 20;
            let z: number = x + y;
            println(z);
            """.trimIndent()

        val output = StringBuilderOutput()
        val pipeline = StreamPipeline(PrintScriptVersion.V1_0, output = output)
        val result = pipeline.run(StringReader(code))

        assertTrue(result is PipelineResult.Success)
        assertEquals("30", output.toString())
    }

    @Test
    fun `test pipeline runs v1_1 program with if else and env`() {
        val code =
            """
            const name: string = readEnv("USER_NAME");
            const condition: boolean = true;
            if (condition) {
                println(name);
            } else {
                println("nobody");
            }
            """.trimIndent()

        val output = StringBuilderOutput()
        val envProvider = MockEnvProvider(mapOf("USER_NAME" to "Alice"))
        val pipeline =
            StreamPipeline(
                PrintScriptVersion.V1_1,
                output = output,
                envProvider = envProvider,
            )
        val result = pipeline.run(StringReader(code))

        assertTrue(result is PipelineResult.Success)
        assertEquals("Alice", output.toString())
    }

    @Test
    fun `test pipeline returns syntax error on invalid syntax`() {
        val code = "let x number = 10;" // missing colon
        val output = StringBuilderOutput()
        val pipeline = StreamPipeline(PrintScriptVersion.V1_0, output = output)
        val result = pipeline.run(StringReader(code))

        assertTrue(result is PipelineResult.SyntaxFailure)
    }

    @Test
    fun `test pipeline returns semantic error on type mismatch`() {
        val code = "let x: number = \"not a number\";"
        val output = StringBuilderOutput()
        val pipeline = StreamPipeline(PrintScriptVersion.V1_0, output = output)
        val result = pipeline.run(StringReader(code))

        assertTrue(result is PipelineResult.SemanticFailure)
    }

    @Test
    fun `test pipeline handles 40k lines without memory issues`() {
        // Genera 40.000 statements de asignación secuencial
        // let x: number = 0;
        // x = x + 1;
        // ... (39998 veces)
        // println(x);
        val count = 40_000
        val sequenceOfCode =
            sequence {
                yield("let x: number = 0;\n")
                repeat(count - 1) {
                    yield("x = 1;\n")
                }
                yield("println(x);\n")
            }

        // Reader que emite caracteres on the fly sin materializar el String en memoria
        val lazyReader =
            object : java.io.Reader() {
                val iterator = sequenceOfCode.iterator()
                var currentStr: String = ""
                var currentPos: Int = 0

                override fun read(
                    cbuf: CharArray,
                    off: Int,
                    len: Int,
                ): Int {
                    if (currentPos >= currentStr.length) {
                        if (!iterator.hasNext()) return -1
                        currentStr = iterator.next()
                        currentPos = 0
                    }
                    var charsRead = 0
                    while (charsRead < len) {
                        if (currentPos >= currentStr.length) {
                            if (!iterator.hasNext()) break
                            currentStr = iterator.next()
                            currentPos = 0
                        }
                        val available = currentStr.length - currentPos
                        val toCopy = minOf(available, len - charsRead)
                        currentStr.toCharArray(cbuf, off + charsRead, currentPos, currentPos + toCopy)
                        currentPos += toCopy
                        charsRead += toCopy
                    }
                    return if (charsRead == 0) -1 else charsRead
                }

                override fun close() = Unit
            }

        val output = StringBuilderOutput()
        val pipeline = StreamPipeline(PrintScriptVersion.V1_0, output = output)
        val result = pipeline.run(lazyReader)

        assertTrue(result is PipelineResult.Success)
        assertEquals("1", output.toString())
    }
}
