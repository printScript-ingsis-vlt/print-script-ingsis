package recurses

import com.sun.jdi.Value

data class Variable(val type: String, var value: Value?)

class Environment { //--> Guarda variable y su referencia {("x", recurses.Variable(Number, 5))}, para no mutar el arbol al realizar una asignacion
    private val variables = mutableMapOf<String, Variable>()
    fun declare(name: String, variable: Variable) { variables[name] = variable }
    fun lookup(name: String): Variable? = variables[name] // --> Busca si existe una variable declarada, medio bot el nombre
}
