package austral.src.main.kotlin.interpreter

data class Variable(val type: String, var value: Value?)

class Environment { //--> Guarda variable y su referencia {("x", Variable(Number, 5))}, para no mutar el arbol muto esto
    private val variables = mutableMapOf<String, Variable>()
    fun declare(name: String, variable: Variable) { variables[name] = variable }
    fun lookup(name: String): Variable? = variables[name] // --> Busca si existe una variable declarada, medio bot el nombre
}