import recurses.Assignment
import recurses.Environment
import recurses.Position
import recurses.PrintStatement
import recurses.Program
import recurses.Variable
import recurses.VariableDeclaration
import recurses.valuedataclass.NumberValue
import recurses.valuedataclass.StringValue
import result.SemanticError

// --> Hace todas las validaciones semanticas, construyendo el environment
class SemanticAnalyzer(private val rules: List<SemanticRule>) {
    fun analyze(program: Program): List<SemanticError> {
        val environment = Environment()
        val errors = mutableListOf<SemanticError>()
        for (stmt in program.statements) {
            // --> Ejecuta todas las reglas sobre el statement actual
            errors.addAll(rules.flatMap { it.check(stmt, environment) })
            // --> Si no hubo errores en esta sentencia, actualiza el Environment
            if (!hasError(stmt.position, errors)) {
                when (stmt) {
                    is VariableDeclaration -> {
                        vdValueDeclaration(stmt, environment)
                    }
                    is Assignment -> {
                        assiValueDeclaration(environment, stmt)
                    }
                    is PrintStatement -> {
                        // --> No modifica el environment
                    }
                }
            }
        }
        return errors
    }

    private fun assiValueDeclaration(
        environment: Environment,
        stmt: Assignment,
    ) {
        val variable = environment.lookup(stmt.name)
        if (variable != null) {
            val assignedValue = if (variable.type == "number") NumberValue(0.0) else StringValue("")
            environment.assign(stmt.name, assignedValue)
        }
    }

    private fun vdValueDeclaration(
        stmt: VariableDeclaration,
        environment: Environment,
    ) {
        val initialValue =
            if (stmt.value != null) {
                if (stmt.type == "number") NumberValue(0.0) else StringValue("")
            } else {
                null
            }
        environment.declare(stmt.name, Variable(stmt.type, initialValue))
    }

    private fun hasError(
        position: Position,
        errors: List<SemanticError>,
    ): Boolean = errors.any { it.position == position }
}
