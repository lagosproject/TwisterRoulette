package com.lakescorp.twisterroulette.domain.model

import com.lakescorp.twisterroulette.R

enum class ColorSet(val nameResId: Int) {
    CLASSIC(R.string.color_set_classic),
    EXTENDED(R.string.color_set_extended),
    DARK_EDITION(R.string.color_set_dark),
    CUSTOM(R.string.color_set_custom);

    fun getDefaultColors(): Set<TwisterColor> {
        return when (this) {
            CLASSIC -> setOf(TwisterColor.RED, TwisterColor.BLUE, TwisterColor.YELLOW, TwisterColor.GREEN)
            EXTENDED -> setOf(TwisterColor.RED, TwisterColor.BLUE, TwisterColor.YELLOW, TwisterColor.GREEN, TwisterColor.PURPLE, TwisterColor.ORANGE)
            DARK_EDITION -> setOf(TwisterColor.RED, TwisterColor.BLUE, TwisterColor.YELLOW, TwisterColor.GREEN, TwisterColor.PURPLE, TwisterColor.ORANGE, TwisterColor.BLACK)
            CUSTOM -> setOf(TwisterColor.RED, TwisterColor.BLUE)
        }
    }
}
