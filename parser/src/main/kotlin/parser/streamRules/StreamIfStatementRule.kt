package parser.streamRules

import ast.Expr
import ast.IfStatement
import ast.Stmt
import parser.grammar.StreamTerminals.ELSE
import parser.grammar.StreamTerminals.IF
import parser.grammar.StreamTerminals.LEFT_BRACE
import parser.grammar.StreamTerminals.LEFT_PAREN
import parser.grammar.StreamTerminals.RIGHT_BRACE
import parser.grammar.StreamTerminals.RIGHT_PAREN
import parser.stream.StreamRule
import parser.stream.action
import parser.stream.many
import parser.stream.opt
import parser.stream.seq
import token.Token

class StreamIfStatementRule(
    expression: StreamRule,
    statement: StreamRule,
) {
    private val block: StreamRule = seq(LEFT_BRACE, many(statement), RIGHT_BRACE)

    val rule: StreamRule =
        action(
            seq(IF, LEFT_PAREN, expression, RIGHT_PAREN, block, opt(seq(ELSE, block))),
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val parts = values as List<Any?>

            val ifToken = parts[IF_TOKEN_INDEX] as Token
            val condition = parts[CONDITION_INDEX] as Expr
            val thenBlock = parts[THEN_BLOCK_INDEX] as List<*>
            val elseClause = parts[ELSE_CLAUSE_INDEX] as List<*>?

            IfStatement(
                condition = condition,
                thenBranch = statementsOf(thenBlock),
                elseBranch = elseClause?.let { statementsOf(it[ELSE_BLOCK_INDEX] as List<*>) },
                position = ifToken.start,
            )
        }

    @Suppress("UNCHECKED_CAST")
    private fun statementsOf(block: List<*>): List<Stmt> = block[STATEMENTS_INDEX] as List<Stmt>

    private companion object {
        const val IF_TOKEN_INDEX = 0
        const val CONDITION_INDEX = 2
        const val THEN_BLOCK_INDEX = 4
        const val ELSE_CLAUSE_INDEX = 5
        const val ELSE_BLOCK_INDEX = 1
        const val STATEMENTS_INDEX = 1
    }
}
