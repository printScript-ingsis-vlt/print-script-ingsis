package austral.src.main.kotlin.interpreter.commun
import austral.src.main.kotlin.interpreter.commun.result.SyntaxError;
import austral.src.main.kotlin.interpreter.commun.result.Result

interface Parser {
    fun parse(line : List<Token>) : Result<Program, List<SyntaxError>>
}