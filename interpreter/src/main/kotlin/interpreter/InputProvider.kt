package interpreter

interface InputProvider {
    fun readLine(): String
}

class StdinInputProvider : InputProvider {
    override fun readLine(): String = readlnOrNull() ?: ""
}

// Para uso programático y unit testing
class MockInputProvider(private val inputs: List<String>) : InputProvider {
    private var index = 0

    override fun readLine(): String {
        if (index >= inputs.size) {
            error("No hay más entradas configuradas en MockInputProvider")
        }
        return inputs[index++]
    }
}
