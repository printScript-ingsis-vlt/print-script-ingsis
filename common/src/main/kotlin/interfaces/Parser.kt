package austral.src.main.kotlin.commun

import common.src.main.kotlin.result.Result
import common.src.main.kotlin.result.SyntaxError
import recurses.Program
import recurses.Token

interface Parser {
    fun parse(line : List<Token>) : Result<Program, List<SyntaxError>>
}