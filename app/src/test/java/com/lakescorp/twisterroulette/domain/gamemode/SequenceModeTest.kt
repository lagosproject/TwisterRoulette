package com.lakescorp.twisterroulette.domain.gamemode

import com.lakescorp.twisterroulette.domain.model.TwisterColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SequenceModeTest {

    @Test
    fun testSequenceModeNoConsecutiveSameBodyPart() {
        val enabledColors = setOf(TwisterColor.RED, TwisterColor.BLUE, TwisterColor.YELLOW, TwisterColor.GREEN)
        // Repeat multiple times to ensure robustness against randomness
        repeat(100) {
            val sequenceMode = SequenceMode(enabledColors, length = 10)
            val turn = sequenceMode.nextTurn()
            val moves = turn.moves
            assertEquals(10, moves.size)
            for (i in 0 until moves.size - 1) {
                assertNotEquals(
                    "Consecutive moves at indices $i and ${i + 1} must not have the same body part",
                    moves[i].bodyPart,
                    moves[i + 1].bodyPart
                )
            }
        }
    }
}
