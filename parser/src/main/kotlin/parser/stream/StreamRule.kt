package parser.stream

import parser.engine.TokenCursor

interface StreamRule {
    fun parse(cursor: TokenCursor): StreamParseResult
}
