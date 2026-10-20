package parser.engine

import token.Token

class ListTokenCursor(
    private val tokens: List<Token>,
) : TokenCursor {
    private var index = 0
    private val activeMarks = mutableListOf<ListMark>()

    override fun peek(offset: Int): Token? {
        val i = index + offset
        return if (i in tokens.indices) tokens[i] else null
    }

    override fun advance(n: Int): Token? {
        require(n >= 0) { "advance count must be >= 0" }
        index = (index + n).coerceAtMost(tokens.size)
        return peek()
    }

    override fun position(): Int = index

    override fun mark(): Mark {
        val m = ListMark(index)
        activeMarks.add(m)
        return m
    }

    override fun reset(mark: Mark) {
        require(mark is ListMark) { "Mark does not belong to this cursor" }
        require(activeMarks.contains(mark)) { "Mark was already released" }
        index = mark.position
    }

    override fun release(mark: Mark) {
        require(mark is ListMark) { "Mark does not belong to this cursor" }
        activeMarks.remove(mark)
    }

    private class ListMark(override val position: Int) : Mark
}

/**
 * Cursor con buffer limitado que consume de un Iterator.
 * Solo retiene en memoria los tokens desde el mark más antiguo
 * hasta el look-ahead máximo usado.
 */
class BufferedTokenCursor(
    private val source: Iterator<Token>,
    private val initialCapacity: Int = 256,
) : TokenCursor {
    /** Buffer circular lógico: tokens[bufferStart .. bufferStart+size) */
    private val buffer = ArrayDeque<Token>(initialCapacity)

    /** Posición absoluta del primer token que todavía está en el buffer */
    private var absoluteBase = 0

    /** Índice relativo dentro del buffer (0 = buffer.first()) */
    private var relativeIndex = 0

    /** Marks activos (posiciones absolutas) */
    private val activeMarks = mutableListOf<BufferedMark>()

    override fun peek(offset: Int): Token? {
        require(offset >= 0) { "peek offset must be >= 0" }
        ensureAvailable(relativeIndex + offset + 1)
        val i = relativeIndex + offset
        return if (i < buffer.size) buffer.elementAt(i) else null
    }

    override fun advance(n: Int): Token? {
        require(n >= 0) { "advance count must be >= 0" }
        ensureAvailable(relativeIndex + n + 1)
        relativeIndex += n
        compactIfPossible()
        return peek()
    }

    override fun position(): Int = absoluteBase + relativeIndex

    override fun mark(): Mark {
        val m = BufferedMark(position())
        activeMarks.add(m)
        return m
    }

    override fun reset(mark: Mark) {
        require(mark is BufferedMark) { "Mark does not belong to this cursor" }
        require(activeMarks.contains(mark)) { "Mark was already released" }
        val targetAbsolute = mark.position
        require(targetAbsolute >= absoluteBase) {
            "Cannot reset to a position that was already discarded from the buffer"
        }
        relativeIndex = targetAbsolute - absoluteBase
    }

    override fun release(mark: Mark) {
        require(mark is BufferedMark) { "Mark does not belong to this cursor" }
        activeMarks.remove(mark)
        compactIfPossible()
    }

    /**
     * Asegura que el buffer tenga al menos [needed] tokens
     * a partir de relativeIndex (pide más al source si hace falta).
     */
    private fun ensureAvailable(needed: Int) {
        while (buffer.size < needed && source.hasNext()) {
            buffer.addLast(source.next())
        }
    }

    /**
     * Descarta tokens del frente del buffer que ya no son alcanzables
     * por ningún mark activo ni por el cursor actual.
     */
    private fun compactIfPossible() {
        val minProtected = activeMarks.minOfOrNull { it.position } ?: position()
        val discardCount = (minProtected - absoluteBase).coerceAtLeast(0)
        if (discardCount <= 0) return

        repeat(discardCount) {
            if (buffer.isNotEmpty()) buffer.removeFirst()
        }
        absoluteBase += discardCount
        relativeIndex -= discardCount
        // relativeIndex nunca debería quedar negativo si la lógica es correcta
        check(relativeIndex >= 0) { "Buffer compaction invariant broken" }
    }

    private class BufferedMark(override val position: Int) : Mark
}
