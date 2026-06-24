package com.lakescorp.twisterroulette.domain.gamemode

import com.lakescorp.twisterroulette.R
import com.lakescorp.twisterroulette.domain.model.AppSettings
import com.lakescorp.twisterroulette.domain.model.BodyPart
import com.lakescorp.twisterroulette.domain.model.ChallengeFrequency
import com.lakescorp.twisterroulette.domain.model.GameModeType
import com.lakescorp.twisterroulette.domain.model.GameResult
import com.lakescorp.twisterroulette.domain.model.Turn
import com.lakescorp.twisterroulette.domain.model.TurnVerb
import com.lakescorp.twisterroulette.domain.model.TwisterColor
import kotlin.random.Random


/**
 * Parent abstraction for a game mode. Each implementation decides what a turn
 * produces (one move, several moves, a "lift", or a challenge) and may keep
 * internal state that evolves across turns.
 */
interface GameMode {
    val type: GameModeType
    fun nextTurn(): Turn
}

private fun randomBodyPart(): BodyPart =
    BodyPart.entries[Random.nextInt(BodyPart.entries.size)]

private fun randomColor(pool: List<TwisterColor>): TwisterColor =
    pool[Random.nextInt(pool.size)]

private fun colorPool(enabled: Set<TwisterColor>): List<TwisterColor> =
    if (enabled.isNotEmpty()) enabled.toList() else TwisterColor.entries

/** Standard play: a random color from the enabled set each turn. */
class ClassicMode(enabledColors: Set<TwisterColor>) : GameMode {
    override val type = GameModeType.CLASSIC
    private val pool = colorPool(enabledColors)
    override fun nextTurn() = Turn(moves = listOf(GameResult(randomBodyPart(), randomColor(pool))))
}

/** Every move is always on the same fixed color; only the body part varies. */
class OneColorMode(private val color: TwisterColor) : GameMode {
    override val type = GameModeType.ONE_COLOR
    override fun nextTurn() = Turn(moves = listOf(GameResult(randomBodyPart(), color)))
}

/**
 * Removes a color from the active palette every [turnsPerDrop] turns, down to
 * [minColors]. The turn on which a color is dropped reports it via [Turn.droppedColor].
 */
class ReducingMode(
    initialColors: Set<TwisterColor>,
    private val turnsPerDrop: Int,
    private val loop: Boolean,
    private val minColors: Int = 2
) : GameMode {
    override val type = GameModeType.REDUCING
    private val initialPalette = colorPool(
        if (initialColors.size >= minColors) initialColors else TwisterColor.entries.toSet()
    )
    private var palette = initialPalette.toMutableList()
    private var turnsSinceDrop = 0

    override fun nextTurn(): Turn {
        if (palette.isEmpty()) palette = initialPalette.toMutableList()
        val color = randomColor(palette)
        var dropped: TwisterColor? = null

        turnsSinceDrop++
        if (turnsSinceDrop >= turnsPerDrop) {
            turnsSinceDrop = 0
            when {
                palette.size > minColors -> {
                    // Drop a color that wasn't just used, if possible.
                    val candidates = palette.filter { it != color }.ifEmpty { palette.toList() }
                    dropped = candidates[Random.nextInt(candidates.size)]
                    palette.remove(dropped)
                }
                loop -> palette = initialPalette.toMutableList()
            }
        }
        return Turn(moves = listOf(GameResult(randomBodyPart(), color)), droppedColor = dropped)
    }
}



/** Mostly normal moves, but occasionally a fun challenge instead of a move. */
class ChallengeMode(
    enabledColors: Set<TwisterColor>,
    private val frequency: ChallengeFrequency
) : GameMode {
    override val type = GameModeType.CHALLENGE
    private val pool = colorPool(enabledColors)
    override fun nextTurn(): Turn =
        if (Random.nextInt(100) < frequency.percentage) {
            Turn(challengeResId = CHALLENGES[Random.nextInt(CHALLENGES.size)])
        } else {
            Turn(moves = listOf(GameResult(randomBodyPart(), randomColor(pool))))
        }

    companion object {
        val CHALLENGES = listOf(
            R.string.challenge_hold,
            R.string.challenge_eyes,
            R.string.challenge_swap,
            R.string.challenge_freeze,
            R.string.challenge_one_hand,
            R.string.challenge_swap_hands,
            R.string.challenge_swap_feet,
            R.string.challenge_previous_player_chooses,
            R.string.challenge_all_swap,
            R.string.challenge_elbows_body,
            R.string.challenge_no_bend_knees
        )
    }
}

/** Announces [length] moves to perform in order. */
class SequenceMode(enabledColors: Set<TwisterColor>, private val length: Int) : GameMode {
    override val type = GameModeType.SEQUENCE
    private val pool = colorPool(enabledColors)
    override fun nextTurn(): Turn {
        val count = length.coerceIn(2, 10)
        val moves = ArrayList<GameResult>(count)
        var lastBodyPart: BodyPart? = null
        for (i in 0 until count) {
            val bodyPart = randomBodyPartExcept(lastBodyPart)
            moves.add(GameResult(bodyPart, randomColor(pool)))
            lastBodyPart = bodyPart
        }
        return Turn(moves = moves)
    }

    private fun randomBodyPartExcept(exclude: BodyPart?): BodyPart {
        if (exclude == null) return randomBodyPart()
        val candidates = BodyPart.entries.filter { it != exclude }
        return candidates[Random.nextInt(candidates.size)]
    }
}

object GameModeFactory {
    fun create(settings: AppSettings): GameMode = when (settings.gameMode) {
        GameModeType.CLASSIC -> ClassicMode(settings.enabledColors)
        GameModeType.ONE_COLOR -> OneColorMode(settings.oneColorColor)
        GameModeType.REDUCING -> ReducingMode(
            settings.enabledColors,
            settings.reducingTurnsPerDrop,
            settings.reducingLoop,
            settings.reducingMinColors
        )

        GameModeType.CHALLENGE -> ChallengeMode(settings.enabledColors, settings.challengeFrequency)
        GameModeType.SEQUENCE -> SequenceMode(settings.enabledColors, settings.sequenceLength)
    }
}
