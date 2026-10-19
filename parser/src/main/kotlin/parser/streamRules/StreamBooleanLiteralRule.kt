package parser.streamRules

import ast.BooleanLiteral
import parser.stream.StreamRule
import parser.stream.action
import parser.stream.choice
import parser.stream.token
import token.Token
import token.TokenType

object StreamBooleanLiteralRule {
    val rule: StreamRule =
        choice(
            action(token(TokenType.TRUE)) { t -> BooleanLiteral(true, (t as Token).start) },
            action(token(TokenType.FALSE)) { t -> BooleanLiteral(false, (t as Token).start) },
        )
}
