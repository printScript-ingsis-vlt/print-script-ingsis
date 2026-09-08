import recurses.Position
import java.io.PushbackReader
import java.io.Reader

// --> Representa el estado físico de lectura, donde Position es la posicion del siguiente caracter a leer
class LexerCursor(reader: Reader) {
    private val input = PushbackReader(reader, 1)

    var position = Position(1, 1)
        private set

    fun peek(): Char? { // --> No consume caracter
        val code = input.read()
        if (code == -1) return null // EOF
        input.unread(code) // --> Devuelve el caracter al buffer
        return code.toChar()
    }

    fun read(): Char? { // --> Consume caracter
        val code = input.read()
        if (code == -1) return null // EOF

        val character = code.toChar()
        position =
            if (character == '\n') { // --> Salto de linea
                Position(position.line + 1, 1)
            } else {
                Position(position.line, position.column + 1)
            }

        return character
    }

    fun skipWhitespace() {
        while (peek()?.isWhitespace() == true) read()
    }
}
