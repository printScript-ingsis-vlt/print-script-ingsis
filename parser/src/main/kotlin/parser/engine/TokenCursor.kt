package parser.engine

import token.Token

interface TokenCursor {
    /**
     * Mira el token en la posición actual + [offset] sin consumirlo.
     * offset = 0 → token actual.
     * Devuelve null si no hay más tokens.
     */
    fun peek(offset: Int = 0): Token?

    /**
     * Avanza [n] posiciones y devuelve el token que quedó
     * debajo del cursor *después* de avanzar (puede ser null).
     */
    fun advance(n: Int = 1): Token?

    /**
     * Índice absoluto desde el inicio del stream (cuántos tokens
     * se consumieron de forma definitiva + posición relativa).
     */
    fun position(): Int

    /**
     * Guarda un punto de restauración para backtracking.
     * Mientras existan marks activos, el buffer no puede compactarse
     * por debajo del mark más antiguo.
     */
    fun mark(): Mark

    /**
     * Vuelve el cursor a la posición del [mark].
     * No libera el mark; hay que llamar a [release] cuando ya no se necesita.
     */
    fun reset(mark: Mark)

    /**
     * Indica que el [mark] ya no se va a usar.
     * Permite que el buffer descarte tokens antiguos.
     */
    fun release(mark: Mark)

    /**
     * true si hay al menos un token disponible (peek() != null).
     */
    fun hasNext(): Boolean = peek() != null
}

interface Mark {
    val position: Int
}
