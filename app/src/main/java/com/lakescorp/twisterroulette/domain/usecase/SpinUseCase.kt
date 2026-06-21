package com.lakescorp.twisterroulette.domain.usecase

import com.lakescorp.twisterroulette.domain.model.BodyPart
import com.lakescorp.twisterroulette.domain.model.GameResult
import com.lakescorp.twisterroulette.domain.model.TwisterColor
import javax.inject.Inject
import kotlin.random.Random

class SpinUseCase @Inject constructor() {
    operator fun invoke(enabledColors: Set<TwisterColor>): GameResult {
        val bodyParts = BodyPart.entries.toTypedArray()
        val randomBodyPart = bodyParts[Random.nextInt(bodyParts.size)]
        
        val colorsList = if (enabledColors.isNotEmpty()) {
            enabledColors.toList()
        } else {
            TwisterColor.entries
        }
        val randomColor = colorsList[Random.nextInt(colorsList.size)]
        
        return GameResult(randomBodyPart, randomColor)
    }
}
