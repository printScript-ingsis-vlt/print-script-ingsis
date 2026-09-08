package matcherImplementations

import LexerCursor
import TokenMatcher
import recurses.Position
import recurses.TokenType
import result.LexicalError
import recurses.Token
import result.Result


class OperatorMatcher(
    private val operators: Map<String, TokenType>, // --> Inyecccion de operadores definidos
) : TokenMatcher {
    override val name = "operator"

    init { // --> Se inicializa antes de que el lexer lo instancie, con el fin de realizar
        // validaciones sobre los operadores
        require(operators.keys.all { it.isNotEmpty() }) {
            "Un operador no puede ser vacío"
        }

        require(operators.keys.none { operator ->
            operator.any(Char::isWhitespace)
        }) {
            "Un operador no puede contener espacios"
        }
    }

    override fun canStartWith(character: Char): Boolean =
        operators.keys.any { it.first() == character } // --> Acepta si coincide con un operador o su inicio

    override fun match(
        cursor: LexerCursor,
        start: Position,
    ): Result<Token, LexicalError> {
        val text = StringBuilder()
        text.append(cursor.read()!!)

        while (true) { // --> Bucle solo de operadores
            val next = cursor.peek() ?: break
            val possibleExtension = text.toString() + next // --> Si tras consumir un operador, el siguiente
            // es un operador, lo agrega como uno solo (por ejemplo un caso donde sea = (1, 1) y = (1,2))

            val hasLongerOperator = operators.keys.any { operator ->
                operator.startsWith(possibleExtension)
            } // --> Busca si ese operador extendido existe realmente

            if (!hasLongerOperator) break

            text.append(cursor.read())
        }

        val op = text.toString()
        val type = operators[op]
            ?: return Result.Failure(
                LexicalError(start, "Operador inválido '$op'"),
            ) // --> Comprueba si existe un operador que coincida exactamente con el obtenido

        return Result.Success(
            Token(
                type = type,
                value = op,
                start = start,
                end = cursor.position,
            ),
        )
    }
}
