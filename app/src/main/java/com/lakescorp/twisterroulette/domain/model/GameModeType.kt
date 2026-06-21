package com.lakescorp.twisterroulette.domain.model

import com.lakescorp.twisterroulette.R

/** The selectable game modes. Each maps to a [com.lakescorp.twisterroulette.domain.gamemode.GameMode] engine. */
enum class GameModeType(val nameResId: Int, val descResId: Int) {
    CLASSIC(R.string.mode_classic_name, R.string.mode_classic_desc),
    ONE_COLOR(R.string.mode_one_color_name, R.string.mode_one_color_desc),
    REDUCING(R.string.mode_reducing_name, R.string.mode_reducing_desc),
    CHALLENGE(R.string.mode_challenge_name, R.string.mode_challenge_desc),
    SEQUENCE(R.string.mode_sequence_name, R.string.mode_sequence_desc)
}
