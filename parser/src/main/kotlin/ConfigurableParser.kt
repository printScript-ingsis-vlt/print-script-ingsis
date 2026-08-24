
import engine.ParseResult
import grammar.Grammar
import interfaces.Parser
import recurses.Program
import recurses.Stmt
import recurses.Position
import result.Result
import result.SyntaxError
import recurses.Token
import recurses.TokenType

class ConfigurableParser : Parser {

    override fun parse(line: List<Token>): Result<Program, List<SyntaxError>> {
        val errors = mutableListOf<SyntaxError>()
        val statements = mutableListOf<Stmt>()
        var pos = 0

        while (pos < line.size && line[pos].type != TokenType.EOF) {
            when (val result = Grammar.declaration.parse(line, pos)) {
                is ParseResult.Success -> {
                    statements.add(result.value as Stmt)
                    pos = result.next
                }
                is ParseResult.Failure -> {
                    errors.add(result.error)
                    // sincronización simple
                    while (pos < line.size &&
                        line[pos].type != TokenType.SEMICOLON &&
                        line[pos].type != TokenType.LET &&
                        line[pos].type != TokenType.EOF) {
                        pos++
                    }
                    if (pos < line.size && line[pos].type == TokenType.SEMICOLON) pos++
                }
            }
        }

        return if (errors.isEmpty()) {
            Result.Success(
                Program(
                    position = line.firstOrNull()?.start ?: Position(0, 0),
                    statements = statements
                )
            )
        } else {
            Result.Failure(errors)
        }
    }
}