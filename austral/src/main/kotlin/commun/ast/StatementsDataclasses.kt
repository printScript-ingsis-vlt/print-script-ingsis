package austral.src.main.kotlin.commun.ast

data class VariableDeclaration(
    val name: String,
    val type: String,
    val value: Expr?,
    override val position: Position
) : Stmt

data class BinaryExpression(
    val left: Expr,
    val operator: String,
    val right: Expr,
    override val position: Position
) : Expr

data class PrintStatement(
    val argument: Expr,
    override val position: Position
) : Stmt