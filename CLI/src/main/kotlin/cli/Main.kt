package cli

import com.github.ajalt.clikt.core.subcommands
import commands.FormatCommand
import commands.InterpretCommand
import commands.LexCommand
import commands.LintCommand
import commands.MyLangCli

fun main(args: Array<String>) =
    MyLangCli()
        .subcommands(LexCommand(), InterpretCommand(), FormatCommand(), LintCommand())
        .main(args)
