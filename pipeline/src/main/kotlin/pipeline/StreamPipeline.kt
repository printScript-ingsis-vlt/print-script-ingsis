package pipeline

import ast.PrintScriptVersion
import ast.Stmt
import interpreter.ConfigurableInterpreter
import interpreter.ConsoleOutput
import interpreter.EnvProvider
import interpreter.InterpreterConfigurations
import interpreter.Output
import interpreter.ScriptInputProvider
import interpreter.StdinInputProvider
import interpreter.SystemEnvProvider
import lexer.Lexer
import lexer.LexerConfigurations
import lexer.StreamLexer
import parser.StreamParser
import parser.engine.BufferedTokenCursor
import parser.streamRules.StreamGrammarConfigurations
import result.LexicalError
import result.Result
import result.SyntaxError
import semantic.SemanticAnalyzer
import semantic.SemanticConfigurations
import semantic.SemanticContext
import token.Token
import java.io.Reader

class StreamPipeline(
    private val version: PrintScriptVersion,
    private val output: Output = ConsoleOutput,
    private val inputProvider: ScriptInputProvider = StdinInputProvider(),
    private val envProvider: EnvProvider = SystemEnvProvider(),
) {
    fun run(reader: Reader): PipelineResult {
        val lexerConfig = LexerConfigurations.getConfiguration(version)
        val lexer = StreamLexer(reader, lexerConfig)
        return runWithLexer(lexer)
    }

    fun runWithLexer(lexer: Lexer): PipelineResult {
        val cursor = BufferedTokenCursor(LexerTokenIterator(lexer))
        val parser: StreamParser = StreamGrammarConfigurations.getConfiguration(version)
        val semanticContext = SemanticContext()
        val semanticAnalyzer = SemanticAnalyzer(SemanticConfigurations.getConfiguration(version))
        val interpreter =
            ConfigurableInterpreter(
                output,
                InterpreterConfigurations.getConfiguration(version),
                inputProvider,
                envProvider,
            )

        return try {
            executeStatements(parser.parseStatements(cursor), semanticAnalyzer, semanticContext, interpreter)
        } catch (e: LexicalException) {
            PipelineResult.LexicalFailure(e.error)
        }
    }

    private fun executeStatements(
        statementsSequence: Sequence<Result<Stmt, SyntaxError>>,
        semanticAnalyzer: SemanticAnalyzer,
        semanticContext: SemanticContext,
        interpreter: ConfigurableInterpreter,
    ): PipelineResult {
        for (statementResult in statementsSequence) {
            val failure = processStatement(statementResult, semanticAnalyzer, semanticContext, interpreter)
            if (failure != null) return failure
        }
        return PipelineResult.Success
    }

    private fun processStatement(
        result: Result<Stmt, SyntaxError>,
        semanticAnalyzer: SemanticAnalyzer,
        semanticContext: SemanticContext,
        interpreter: ConfigurableInterpreter,
    ): PipelineResult? =
        when (result) {
            is Result.Failure -> PipelineResult.SyntaxFailure(result.error)
            is Result.Success -> {
                val errors = semanticAnalyzer.analyzeStatement(result.value, semanticContext)
                if (errors.isNotEmpty()) {
                    PipelineResult.SemanticFailure(errors)
                } else {
                    interpreter.execute(result.value)
                    null
                }
            }
        }

    private class LexerTokenIterator(private val lexer: Lexer) : Iterator<Token> {
        override fun hasNext(): Boolean = lexer.hasNext()

        override fun next(): Token {
            if (!hasNext()) throw NoSuchElementException("No more tokens")
            return when (val res = lexer.nextToken()) {
                is Result.Success -> res.value
                is Result.Failure -> throw LexicalException(res.error)
            }
        }
    }

    private class LexicalException(val error: LexicalError) : RuntimeException(error.message)
}
