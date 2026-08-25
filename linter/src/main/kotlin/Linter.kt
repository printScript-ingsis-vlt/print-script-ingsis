import dataclass.LintNotification
import recurses.Program

interface Linter {

    /**
     * Dado un program devuelve las notificaciones de linteo correspondientes
     *
     * @param program AST del programa el cual analizar
     * @return lista de todas las notificaciones (errores o warnings) correspondientes a ese program
     */


    fun lint(program: Program): List<LintNotification>
    // --> En tal caso de no detectar un problema en el nodo dado, devuelve una lista vacia
}
