package commands

import ConfigurableParser
import ConsoleOutput
import Interpreter
import PrintScriptFormatter
import PrintScriptLinter
import StreamLexer
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import result.Result

class MyLangCli : CliktCommand(name = "mylang") {
    override fun run() = Unit // solo agrupa subcomandos
}

class LexCommand : CliktCommand(name = "lex") {
    val file by argument().file(mustExist = true)

    override fun run() {
        when (val result = StreamLexer.tokenize(file.bufferedReader())) {
            is Result.Success -> result.value.forEach(::println)
            is Result.Failure -> echo("Error léxico: ${result.error}", err = true)
        }
    }
}



class InterpretCommand : CliktCommand(
    name = "run",
    help = "Interpreta el archivo",
) {
    private val file by argument().file(mustExist = true)

    override fun run() {
        val tokens = when (val result =
            StreamLexer.tokenize(file.bufferedReader())) {
            is Result.Success -> result.value
            is Result.Failure -> {
                echo("Error léxico: ${result.error.message}", err = true)
                return
            }
        }

        val program = when (val result = ConfigurableParser().parse(tokens)) {
            is Result.Success -> result.value
            is Result.Failure -> {
                result.error.forEach { error ->
                    echo("Error de sintaxis: ${error.message}", err = true)
                }
                return
            }
        }

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
        val tokens = when (val result =
            StreamLexer.tokenize(file.bufferedReader())) {
            is Result.Success -> result.value
            is Result.Failure -> {
                echo("Error léxico: ${result.error.message}", err = true)
                return
            }
        }

        val program = when (val result = ConfigurableParser().parse(tokens)) {
            is Result.Success -> result.value
            is Result.Failure -> {
                result.error.forEach { echo("Error de sintaxis:${it.message}", err = true) }
                    return
                }
            }

                val formatted = PrintScriptFormatter(
                FormattingConfigLoader.loadDefault(),
            ).format(program)

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
        val tokens = when (val result =
            StreamLexer.tokenize(file.bufferedReader())) {
            is Result.Success -> result.value
            is Result.Failure -> {
                echo("Error léxico: ${result.error.message}", err = true)
                return
            }
        }

        val program = when (val result = ConfigurableParser().parse(tokens)) {
            is Result.Success -> result.value
            is Result.Failure -> {
                result.error.forEach { echo("Error de sintaxis:${it.message}", err = true) }
                    return
                }
            }

                val notifications = PrintScriptLinter().lint(program)

            if (notifications.isEmpty()) {
                echo("No se encontraron problemas.")
                return
            }

                notifications.forEach { notification ->
                echo(
                    "${notification.severity}: " +
                        "${notification.position.line}: ${notification.position.column} - " +
                        notification.message,
                )
            }
        }
    }
