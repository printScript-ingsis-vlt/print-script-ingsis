package parser.streamRules

import ast.PrintScriptVersion
import ast.VersionConfigurationProvider
import parser.StreamParser
import parser.grammar.StreamTerminals
import parser.stream.Choice
import parser.stream.StreamRule
import parser.stream.ref
import parser.streamParserOf

object StreamGrammarConfigurations : VersionConfigurationProvider<StreamParser> {
    private val expression = StreamExpressionRule().expression
    private val expressionV1_1 = buildExpressionV11()

    private fun buildExpressionV11(): StreamRule {
        lateinit var expr: StreamRule
        val expressionRef: StreamRule = ref { expr }

        expr =
            StreamExpressionRule(
                extraPrimaries =
                    listOf(
                        StreamBooleanLiteralRule.rule,
                        StreamReadInputExpressionRule(expressionRef).rule,
                        StreamReadEnvExpressionRule(expressionRef).rule,
                    ),
            ).expression

        return expr
    }

    val v1_0: StreamParser =
        streamParserOf(
            listOf(
                StreamDeclarationRule(expression).rule,
                StreamAssignmentRule(expression).rule,
                StreamPrintStatementRule(expression).rule,
            ),
        )

    val v1_1: StreamParser = buildV11()

    private fun buildV11(): StreamParser {
        lateinit var statementRules: List<StreamRule>
        val statementDispatcher: StreamRule = ref { Choice(statementRules) }

        statementRules =
            listOf(
                StreamDeclarationRule(expressionV1_1, extraTypeTokens = listOf(StreamTerminals.BOOLEAN)).rule,
                StreamConstDeclarationRule(expressionV1_1, extraTypeTokens = listOf(StreamTerminals.BOOLEAN)).rule,
                StreamAssignmentRule(expressionV1_1).rule,
                StreamPrintStatementRule(expressionV1_1).rule,
                StreamIfStatementRule(expressionV1_1, statementDispatcher).rule,
            )

        return streamParserOf(statementRules)
    }

    val default: StreamParser = v1_1

    override fun getConfiguration(version: PrintScriptVersion): StreamParser =
        when (version) {
            PrintScriptVersion.V1_0 -> v1_0
            PrintScriptVersion.V1_1 -> v1_1
        }
}
