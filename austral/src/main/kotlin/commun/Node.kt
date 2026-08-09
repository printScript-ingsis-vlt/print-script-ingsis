package austral.src.main.kotlin.commun

sealed interface Node {
    val position: Position
}

sealed interface Stmt : Node  // -> Lo que nosotroso vamos a llamar funciones
sealed interface Expr : Node  // -> lo que son expresoines a resolver