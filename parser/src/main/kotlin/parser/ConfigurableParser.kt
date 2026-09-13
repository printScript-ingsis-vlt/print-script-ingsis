package parser

import ast.Position
import ast.Program
import ast.Stmt
import parser.engine.Choice
import parser.engine.ParseResult
import parser.engine.Rule
import parser.grammar.GrammarConfiguration
import parser.grammar.GrammarConfigurations
import result.Result
import result.SyntaxError
import token.Token
import token.TokenType

class ConfigurableParser(
    configuration: GrammarConfiguration = GrammarConfigurations.default,
) : Parser {
    private val statement: Rule = Choice(configuration.statementRules.map { it.rule })

    override fun parse(line: List<Token>): Result<Program, List<SyntaxError>> {
        val errors = mutableListOf<SyntaxError>()
        val statements = mutableListOf<Stmt>()
        var pos = 0

        while (pos < line.size && line[pos].type != TokenType.EOF) {
            when (val result = statement.parse(line, pos)) {
                is ParseResult.Success -> {
                    statements.add(result.value as Stmt)
                    pos = result.next
                }
                is ParseResult.Failure -> {
                    errors.add(result.error)

                    while (
                        pos < line.size &&
                        line[pos].type != TokenType.SEMICOLON &&
                        line[pos].type != TokenType.EOF
                    ) {
                        pos++
                    }
                    if (pos < line.size && line[pos].type == TokenType.SEMICOLON) {
                        pos++
                    }
                }
            }
        }

        return if (errors.isEmpty()) {
            Result.Success(
                Program(
                    position = line.firstOrNull()?.start ?: Position(0, 0),
                    statements = statements,
                ),
            )
        } else {
            Result.Failure(errors)
        }
    }
}
