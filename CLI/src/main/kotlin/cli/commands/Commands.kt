package cli.commands

import ast.Program
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import formatter.FormattingConfigLoader
import formatter.PrintScriptFormatter
import interpreter.ConsoleOutput
import interpreter.Interpreter
import lexer.StreamLexer
import linter.PrintScriptLinter
import parser.ConfigurableParser
import result.Result
import java.io.File

class MyLangCli : CliktCommand(name = "mylang") {
    override fun run() = Unit
}

class LexCommand : CliktCommand(name = "lex") {
    private val file by argument().file(mustExist = true)

    override fun run() {
        when (val result = file.bufferedReader().use { StreamLexer.tokenize(it) }) {
            is Result.Success -> result.value.forEach(::println)
            is Result.Failure -> echo("Error léxico: ${result.error.message}", err = true)
        }
    }
}

class InterpretCommand : CliktCommand(
    name = "run",
    help = "Interpreta el archivo",
) {
    private val file by argument().file(mustExist = true)

    override fun run() {
        val program = loadProgram(file) ?: return
        Interpreter(ConsoleOutput).run(program)
    }
}

class FormatCommand : CliktCommand(
    name = "fmt",
    help = "Formatea el archivo",
) {
    private val file by argument().file(mustExist = true)
    private val write by option("-w", "--write", help = "Sobreescribe el archivo").flag()

    override fun run() {
        val program = loadProgram(file) ?: return
        val formatted = PrintScriptFormatter(FormattingConfigLoader.loadDefault()).format(program)

        if (write) {
            file.writeText(formatted)
            echo("Archivo formateado: ${file.path}")
        } else {
            echo(formatted)
        }
    }
}

class LintCommand : CliktCommand(
    name = "lint",
    help = "Corre el linter",
) {
    private val file by argument().file(mustExist = true)

    override fun run() {
        val program = loadProgram(file) ?: return
        val notifications = PrintScriptLinter().lint(program)

        if (notifications.isEmpty()) {
            echo("No se encontraron problemas.")
        } else {
            notifications.forEach { notification ->
                echo(
                    "${notification.severity}: " +
                        "${notification.position.line}:${notification.position.column} - " +
                        notification.message,
                )
            }
        }
    }
}

private fun CliktCommand.loadProgram(file: File): Program? {
    val tokens =
        when (val result = file.bufferedReader().use { StreamLexer.tokenize(it) }) {
            is Result.Success -> result.value
            is Result.Failure -> {
                echo("Error léxico: ${result.error.message}", err = true)
                return null
            }
        }

    return when (val result = ConfigurableParser().parse(tokens)) {
        is Result.Success -> result.value
        is Result.Failure -> {
            result.error.forEach { error ->
                echo("Error de sintaxis: ${error.message}", err = true)
            }
            null
        }
    }
}
