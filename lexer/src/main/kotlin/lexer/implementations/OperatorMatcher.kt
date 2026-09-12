package lexer.implementations

import ast.Position
import lexer.LexerCursor
import lexer.TokenMatcher
import result.LexicalError
import result.Result
import token.Token
import token.TokenType

class OperatorMatcher(
    // Inyección de los operadores definidos por el lenguaje.
    private val operators: Map<String, TokenType>,
) : TokenMatcher {
    override val name = "operator"

    init { // --> Se inicializa antes de que el lexer lo instancie, con el fin de realizar
        // validaciones sobre los operadores
        require(operators.keys.all { it.isNotEmpty() }) {
            "Un operador no puede ser vacío"
        }

        require(
            operators.keys.none { operator ->
                operator.any(Char::isWhitespace)
            },
        ) {
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
            val possibleExtension = cursor.peek()?.let { text.toString() + it }

            if (
                possibleExtension == null ||
                operators.keys.none { operator -> operator.startsWith(possibleExtension) }
            ) {
                break
            }

            text.append(cursor.read()!!)
        }

        val op = text.toString()
        val type =
            operators[op]
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
