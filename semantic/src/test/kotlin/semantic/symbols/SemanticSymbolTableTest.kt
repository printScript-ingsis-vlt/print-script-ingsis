package semantic.symbols

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SemanticSymbolTableTest {
    @Test
    fun `declares and retrieves a symbol`() {
        val table = SemanticSymbolTable()
        val symbol = SemanticSymbol("number", initialized = true, knownNumberValue = 3.0)

        table.declare("count", symbol)

        assertEquals(symbol, table.lookup("count"))
    }

    @Test
    fun `assignment preserves symbol properties and updates known value`() {
        val table = SemanticSymbolTable()
        table.declare("count", SemanticSymbol("number", initialized = false, mutable = false))

        table.assign("count", 8.0)

        assertEquals(
            SemanticSymbol("number", initialized = true, mutable = false, knownNumberValue = 8.0),
            table.lookup("count"),
        )
    }

    @Test
    fun `nested scope shadows outer symbol and is discarded on exit`() {
        val table = SemanticSymbolTable()
        table.declare("value", SemanticSymbol("number", initialized = true, knownNumberValue = 1.0))
        table.enterScope()
        table.declare("value", SemanticSymbol("string", initialized = true))

        assertEquals("string", table.lookup("value")?.type)

        table.exitScope()

        assertEquals("number", table.lookup("value")?.type)
    }

    @Test
    fun `cannot exit the global scope`() {
        val table = SemanticSymbolTable()

        assertThrows(IllegalArgumentException::class.java) { table.exitScope() }
        assertNull(table.lookup("missing"))
    }
}
