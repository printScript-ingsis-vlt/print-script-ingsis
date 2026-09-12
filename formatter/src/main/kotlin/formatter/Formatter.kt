package formatter

import ast.Program

interface Formatter {
    /**
     * Formatea un programa PrintScript según las reglas de formato.
     *
     * @param program AST del programa a formatear
     * @return Código formateado como String
     */
    fun format(program: Program): String
}
