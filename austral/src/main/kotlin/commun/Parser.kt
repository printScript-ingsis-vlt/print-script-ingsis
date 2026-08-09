package austral.src.main.kotlin.commun

interface Parser {
    fun parse(line : List<Token>) : Node
}