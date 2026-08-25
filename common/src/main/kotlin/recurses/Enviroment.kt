package recurses

import com.sun.jdi.Value

data class Variable(val type: String, var value: Value?)

class Environment {
    // Guarda variable y su referencia {("x", recurses.Variable(Number, 5))}
    // para no mutar el árbol al realizar una asignación
    private val variables = mutableMapOf<String, Variable>()

    fun declare(
        name: String,
        variable: Variable,
    ) {
        variables[name] = variable
    }

    // Busca si existe una variable declarada, medio bot el nombre
    fun lookup(name: String): Variable? = variables[name]
}
