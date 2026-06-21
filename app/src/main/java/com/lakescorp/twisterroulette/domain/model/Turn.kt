package com.lakescorp.twisterroulette.domain.model

/** Whether the turn asks players to place a limb or to lift one off. */
enum class TurnVerb { PLACE, LIFT }

/**
 * The output of a single game turn. Most modes produce one [moves] entry with
 * [TurnVerb.PLACE]; richer modes use the extra fields:
 *  - Sequence -> several [moves] to perform in order
 *  - Challenge-> [challengeResId] set (a task instead of a move)
 *  - Reducing -> [droppedColor] set on the turn a color is removed
 */
data class Turn(
    val moves: List<GameResult> = emptyList(),
    val verb: TurnVerb = TurnVerb.PLACE,
    val challengeResId: Int? = null,
    val droppedColor: TwisterColor? = null
) {
    val primaryMove: GameResult? get() = moves.firstOrNull()
}
