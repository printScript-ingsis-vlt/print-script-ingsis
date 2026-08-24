package interfaces

import recurses.Program
import result.Result
import result.SyntaxError
import recurses.Token

interface Parser {
    fun parse(line : List<Token>) : Result<Program, List<SyntaxError>>
}