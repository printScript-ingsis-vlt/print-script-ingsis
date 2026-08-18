package austral.src.main.kotlin.commun
import austral.src.main.kotlin.commun.ast.Program
import austral.src.main.kotlin.commun.result.SyntaxError;
import austral.src.main.kotlin.commun.result.Result

interface Parser {
    fun parse(line : List<Token>) : Result<Program, List<SyntaxError>>
}