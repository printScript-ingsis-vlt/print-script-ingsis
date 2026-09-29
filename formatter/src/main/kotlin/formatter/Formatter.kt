package formatter

import ast.Program
import token.Token

interface Formatter {
    /**
     * Formatea un programa PrintScript según las reglas de formato.
     *
     * @param tokens tokens originales del archivo (para preservar espaciado cuando una
     *   regla viene en null)
     * @param program AST del programa a formatear
     * @return Código formateado como String
     */
    fun format(
        tokens: List<Token>,
        program: Program,
    ): String
}
