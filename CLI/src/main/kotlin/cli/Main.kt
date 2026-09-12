package cli

import cli.commands.FormatCommand
import cli.commands.InterpretCommand
import cli.commands.LexCommand
import cli.commands.LintCommand
import cli.commands.MyLangCli
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) =
    MyLangCli()
        .subcommands(LexCommand(), InterpretCommand(), FormatCommand(), LintCommand())
        .main(args)
