import austral.src.main.kotlin.commun.Parser
import common.src.main.kotlin.result.Result
import common.src.main.kotlin.result.SyntaxError
import recurses.Program
import recurses.Stmt
import recurses.BinaryExpression
import recurses.Expr
import recurses.Identifier
import recurses.NumberLiteral
import recurses.Position
import recurses.PrintStatement
import recurses.StringLiteral
import recurses.Token
import recurses.TokenType
import recurses.VariableDeclaration


class RecursiveDescentParser : Parser {
    private lateinit var tokens: List<Token>
    private var current = 0
    private val errors = mutableListOf<SyntaxError>()

    override fun parse(tokens: List<Token>): Result<Program, List<SyntaxError>> {
        this.tokens = tokens
        this.current = 0
        errors.clear()

        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            try {
                statements.add(parseStatement())
            } catch (e: ParseException) {
                errors.add(SyntaxError(e.token.start, e.message ?: "Syntax error"))
                synchronize()
            }
        }

        return if (errors.isEmpty()) {
            Result.Success(
                Program(
                    position = if (tokens.isNotEmpty()) tokens[0].start else Position(0, 0),
                    statements = statements
                )
            )
        } else {
            Result.Failure(errors.toList())
        }
    }

    // excepción SOLO interna — nunca sale de esta clase
    private class ParseException(val token: Token, message: String) : Exception(message)

    // ---------- helpers de bajo nivel ----------

    private fun peek(): Token = tokens[current]
    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF
    private fun advance(): Token = tokens[current].also { if (!isAtEnd()) current++ }
    private fun check(type: TokenType): Boolean = !isAtEnd() && peek().type == type

    private fun match(vararg types: TokenType): Boolean {
        if (types.any { check(it) }) { advance(); return true }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw ParseException(peek(), message)
    }

    // salta tokens hasta un punto seguro para reintentar el próximo statement
    private fun synchronize() {
        while (!isAtEnd()) {
            if (tokens[current - 1].type == TokenType.SEMICOLON) return
            if (peek().type == TokenType.LET) return
            advance()
        }
    }

    // ---------- statement = declaration | assignment | call ----------

    private fun parseStatement(): Stmt {
        if (match(TokenType.LET)) return variableDeclaration()
        else if (check(TokenType.IDENTIFIER) && peek().value.equals("printLn")) return printStatement()
        throw ParseException(peek(), "Expected statement")
    }

    // ---------- declaration = "let", identifier, ":", type, ["=", expression], ";" ----------

    private fun variableDeclaration(): Stmt {
        val letToken = tokens[current - 1]
        val name = consume(TokenType.IDENTIFIER, "Expected variable name")
        consume(TokenType.COLON, "Expected ':' after variable name")
        val type = consume(TokenType.IDENTIFIER, "Expected type")

        var value: Expr? = null
        if (match(TokenType.EQUAL)) {
            value = expression()
        }
        consume(TokenType.SEMICOLON, "Expected ';' after declaration")

        return VariableDeclaration(name.value, type.value, value, letToken.start)
    }

    private fun printStatement(): Stmt {
        val nameToken = advance()
        consume(TokenType.LEFT_PAREN, "Expected '(' before expression")
        val arg = expression()
        consume(TokenType.LEFT_PAREN, "Expected ')' after expression")
        consume(TokenType.SEMICOLON, "Expected ';' after statement")
        return PrintStatement(arg, nameToken.start)
    }

    // ---------- expression = term, {("+"|"-"), term} ----------

    private fun expression(): Expr {
        var expr = term()
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = tokens[current - 1]
            expr = BinaryExpression(expr, op.value, term(), op.start)
        }
        return expr
    }

    // ---------- term = factor, {("*"|"/"), factor} ----------

    private fun term(): Expr {
        var expr = factor()
        while (match(TokenType.STAR, TokenType.SLASH)) {
            val op = tokens[current - 1]
            expr = BinaryExpression(expr, op.value, factor(), op.start)
        }
        return expr
    }

    // ---------- factor = number | string | identifier | "(" expression ")" ----------

    private fun factor(): Expr {
        val token = peek()
        if (match(TokenType.NUMBER_LITERAL)) return NumberLiteral(token.value.toDouble(), token.start)
        if (match(TokenType.STRING_LITERAL)) return StringLiteral(token.value, token.start)
        if (match(TokenType.IDENTIFIER)) return Identifier(token.value, token.start)
        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression")
            return expr
        }
        throw ParseException(token, "Expected expression, got '${token.value}'")
    }
}