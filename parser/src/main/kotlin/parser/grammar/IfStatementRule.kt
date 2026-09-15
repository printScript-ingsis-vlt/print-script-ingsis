package parser.grammar

import ast.Expr
import ast.IfStatement
import ast.Stmt
import parser.engine.Rule
import parser.engine.action
import parser.engine.many
import parser.engine.opt
import parser.engine.seq
import parser.grammar.Terminals.ELSE
import parser.grammar.Terminals.IF
import parser.grammar.Terminals.LEFT_BRACE
import parser.grammar.Terminals.LEFT_PAREN
import parser.grammar.Terminals.RIGHT_BRACE
import parser.grammar.Terminals.RIGHT_PAREN
import token.Token

/**
 * if '(' expression ')' '{' statement* '}' ('else' '{' statement* '}')?
 *
 * No soporta "else if": tras 'else' se exige '{' directo, nunca otro 'if', asi que
 * "else if (...)" falla de forma natural sin necesidad de un caso especial.
 *
 * [statement] es una referencia perezosa (Ref) al dispatcher completo de statements de la
 * version (incluida esta misma regla), asi los bloques pueden anidar cualquier statement,
 * incluyendo otro if, sin que IfStatementRule necesite conocer esa lista de antemano.
 */
class IfStatementRule(
    expression: Rule,
    statement: Rule,
) : StatementRule {
    private val block: Rule = seq(LEFT_BRACE, many(statement), RIGHT_BRACE)

    override val rule: Rule =
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

    // el bloque parseado es [LEFT_BRACE, List<Stmt>, RIGHT_BRACE], solo nos interesa el medio
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
