package interfaces

import recurses.Program
import recurses.Token
import result.Result
import result.SyntaxError

interface Parser {
    fun parse(line: List<Token>): Result<Program, List<SyntaxError>>
}
