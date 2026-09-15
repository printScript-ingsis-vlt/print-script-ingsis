package semantic.symbols

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
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

    @Test
    fun `copy isolates assignments from the original table`() {
        val original = SemanticSymbolTable()
        original.declare("count", SemanticSymbol("number", initialized = false))

        val copy = original.copy()
        copy.assign("count", 1.0)

        assertEquals(false, original.lookup("count")?.initialized)
        assertEquals(1.0, copy.lookup("count")?.knownNumberValue)
    }

    @Test
    fun `with scope removes local symbols after executing the block`() {
        val table = SemanticSymbolTable()

        val result =
            table.withScope {
                table.declare("local", SemanticSymbol("string", initialized = true))
                "block result"
            }

        assertEquals("block result", result)
        assertNull(table.lookup("local"))
    }

    @Test
    fun `merge marks a symbol initialized when both branches initialize it`() {
        val original = SemanticSymbolTable()
        original.declare("count", SemanticSymbol("number", initialized = false))
        val thenSymbols = original.copy()
        val elseSymbols = original.copy()
        thenSymbols.assign("count", 1.0)
        elseSymbols.assign("count", 2.0)

        original.mergeConditionalBranches(thenSymbols, elseSymbols)

        assertTrue(original.lookup("count")?.initialized == true)
        assertNull(original.lookup("count")?.knownNumberValue)
    }

    @Test
    fun `merge preserves an uninitialized symbol when one branch does not assign it`() {
        val original = SemanticSymbolTable()
        original.declare("count", SemanticSymbol("number", initialized = false))
        val thenSymbols = original.copy()
        val elseSymbols = original.copy()
        thenSymbols.assign("count", 1.0)

        original.mergeConditionalBranches(thenSymbols, elseSymbols)

        assertEquals(false, original.lookup("count")?.initialized)
        assertNull(original.lookup("count")?.knownNumberValue)
    }
}
