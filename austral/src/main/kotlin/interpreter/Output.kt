package austral.src.main.kotlin.interpreter

interface Output { fun write(text: String) } // --> Con esto se donde escribir cuando el programa imprima
// sin hacerlo directo en consola para poder testear outputs despues. capaz no esta muy bueno

object ConsoleOutput : Output {
    override fun write(text: String) = print(text)
}

object NullOutput : Output {
    override fun write(text: String) = Unit
}

class StringBuilderOutput : Output {
    private val buffer = StringBuilder()
    override fun write(text: String) { buffer.append(text) }
    override fun toString(): String = buffer.toString()
}