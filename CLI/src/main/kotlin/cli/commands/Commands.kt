package cli.commands

import ast.PrintScriptVersion
import ast.Program
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import formatter.FormattingConfigLoader
import formatter.PrintScriptFormatter
import interpreter.ConfigurableInterpreter
import interpreter.ConsoleOutput
import interpreter.InterpreterConfigurations
import lexer.LexerConfigurations
import lexer.StreamLexer
import linter.PrintScriptLinter
import parser.ConfigurableParser
import parser.grammar.GrammarConfigurations
import result.Result
import semantic.SemanticAnalyzer
import semantic.SemanticConfigurations
import java.io.File

class MyLangCli : CliktCommand(name = "mylang") {
    override fun run() = Unit
}

class LexCommand : CliktCommand(name = "lex") {
    private val file by argument().file(mustExist = true)
    private val versionStr by option("-v", "--version", "--lang-version", help = "Versión del lenguaje (1.0 o 1.1)")
        .default("1.1")

    override fun run() {
        val version = PrintScriptVersion.fromString(versionStr)
        val lexerConfig = LexerConfigurations.getConfiguration(version)

        val tokens =
            when (
                val result =
                    file.bufferedReader().use { reader ->
                        StreamLexer(reader, lexerConfig).tokenize()
                    }
            ) {
                is Result.Success -> result.value
                is Result.Failure -> {
                    echo("Error léxico: ${result.error.message}", err = true)
                    null
                }
            }

        if (tokens != null) {
            tokens.forEach(::println)
        }
    }
}

class InterpretCommand : CliktCommand(
    name = "run",
    help = "Interpreta el archivo",
) {
    private val file by argument().file(mustExist = true)
    private val versionStr by option("-v", "--version", "--lang-version", help = "Versión del lenguaje (1.0 o 1.1)")
        .default("1.1")

    override fun run() {
        val version = PrintScriptVersion.fromString(versionStr)
        val program = loadProgram(file, version) ?: return
        val interpreterConfig = InterpreterConfigurations.getConfiguration(version)
        ConfigurableInterpreter(ConsoleOutput, interpreterConfig).run(program)
    }
}

class FormatCommand : CliktCommand(
    name = "fmt",
    help = "Formatea el archivo",
) {
    private val file by argument().file(mustExist = true)
    private val write by option("-w", "--write", help = "Sobreescribe el archivo").flag()

    override fun run() {
        // Carga el programa con la versión por defecto de PrintScript
        val program = loadProgram(file, PrintScriptVersion.DEFAULT) ?: return
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
        // Carga el programa con la versión por defecto de PrintScript
        val program = loadProgram(file, PrintScriptVersion.DEFAULT) ?: return
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

private fun CliktCommand.loadProgram(
    file: File,
    version: PrintScriptVersion,
): Program? {
    val lexerConfig = LexerConfigurations.getConfiguration(version)

    // 2. Instanciar el Lexer pasándole la configuración seleccionada
    val tokens =
        when (
            val result =
                file.bufferedReader().use { reader ->
                    StreamLexer(reader, lexerConfig).tokenize()
                }
        ) {
            is Result.Success -> result.value
            is Result.Failure -> {
                echo("Error léxico: ${result.error.message}", err = true)
                null
            }
        }

    val parserConfig = GrammarConfigurations.getConfiguration(version)
    val program =
        if (tokens != null) {
            when (val result = ConfigurableParser(parserConfig).parse(tokens)) {
                is Result.Success -> result.value
                is Result.Failure -> {
                    result.error.forEach { error ->
                        echo("Error de sintaxis: ${error.message}", err = true)
                    }
                    null
                }
            }
        } else {
            null
        }

    if (program != null && !validateSemantics(program, version)) {
        return null
    }

    return program
}

private fun CliktCommand.validateSemantics(
    program: Program,
    version: PrintScriptVersion,
): Boolean {
    val semanticConfig = SemanticConfigurations.getConfiguration(version)
    val semanticErrors = SemanticAnalyzer(semanticConfig).analyze(program)

    if (semanticErrors.isEmpty()) return true

    semanticErrors.forEach { error ->
        echo(
            "Error semántico: ${error.position.line}:${error.position.column} - ${error.message}",
            err = true,
        )
    }

    return false
}
