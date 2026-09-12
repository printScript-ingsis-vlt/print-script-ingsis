package semantic

import ast.Position

fun pos(
    line: Int = 1,
    col: Int = 1,
) = Position(line, col)
