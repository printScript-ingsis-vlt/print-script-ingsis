package grammar

import engine.Rule
import engine.action
import engine.choice
import engine.opt
import engine.seq
import engine.token
import recurses.Expr
import recurses.Identifier
import recurses.NumberLiteral
import recurses.StringLiteral
import recurses.Token
import recurses.TokenType
import recurses.VariableDeclaration

object Grammar {

    // ---------- Terminals reutilizables ----------
    val LET = token(TokenType.LET, "let")
    val COLON = token(TokenType.COLON, ":")
    val EQUAL = token(TokenType.EQUAL, "=")
    val SEMICOLON = token(TokenType.SEMICOLON, ";")
    val IDENTIFIER = token(TokenType.IDENTIFIER, "identifier")

    // ---------- expression (simplificado por ahora) ----------
    // Más adelante lo reemplazamos por Pratt o por reglas más completas
    val expression: Rule = action(
        choice(
            token(TokenType.NUMBER_LITERAL),
            token(TokenType.STRING_LITERAL),
            token(TokenType.IDENTIFIER)
            // + paréntesis, etc.
        )
    ) { token ->
        val t = token as Token
        when (t.type) {
            TokenType.NUMBER_LITERAL -> NumberLiteral(t.value.toDouble(), t.start)
            TokenType.STRING_LITERAL -> StringLiteral(t.value, t.start)
            TokenType.IDENTIFIER -> Identifier(t.value, t.start)
            else -> error("unreachable")
        }
    }

    // ---------- declaration completa ----------
    val declaration: Rule = action(
        seq(
            LET,                // 0
            IDENTIFIER,         // 1  → name
            COLON,              // 2
            IDENTIFIER,         // 3  → type
            opt(                // 4  → value (puede ser null)
                seq(EQUAL, expression)
            ),
            SEMICOLON           // 5
        )
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val list = values as List<Any>

        val letToken = list[0] as Token
        val nameToken = list[1] as Token
        val typeToken = list[3] as Token

        val value: Expr? = when (val opt = list[4]) {
            null -> null
            is List<*> -> opt[1] as Expr          // el segundo elemento de [EQUAL, expression]
            else -> null
        }

        VariableDeclaration(
            name = nameToken.value,
            type = typeToken.value,
            value = value,
            position = letToken.start
        )
    }
}