package parser

import ast.Program
import result.Result
import result.SyntaxError
import token.Token

interface Parser {
    fun parse(line: List<Token>): Result<Program, List<SyntaxError>>
}
