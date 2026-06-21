package com.lakescorp.twisterroulette.domain.model

import com.lakescorp.twisterroulette.R

enum class TwisterColor(val nameResId: Int, val hexColor: String) {
    RED(R.string.color_red, "#FF2E2E"),
    BLUE(R.string.color_blue, "#2E8BFF"),
    YELLOW(R.string.color_yellow, "#FFD02E"),
    GREEN(R.string.color_green, "#2EFF5C"),
    PURPLE(R.string.color_purple, "#B42EFF"),
    ORANGE(R.string.color_orange, "#FF7E2E"),
    BLACK(R.string.color_black, "#1E1E1E")
}
