package com.lakescorp.twisterroulette

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.lakescorp.twisterroulette.presentation.previews.PreviewScene_MainIdle
import com.lakescorp.twisterroulette.presentation.previews.PreviewScene_MainResult_Red
import com.lakescorp.twisterroulette.presentation.previews.PreviewScene_MainResult_Yellow
import com.lakescorp.twisterroulette.presentation.previews.PreviewScene_MainTimer_Active
import com.lakescorp.twisterroulette.presentation.previews.PreviewScene_MainVoice_Listening
import com.lakescorp.twisterroulette.presentation.previews.PreviewScene_Modes_Classic
import com.lakescorp.twisterroulette.presentation.previews.PreviewScene_Settings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ScreenshotTest(private val localeTag: String) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("en"),
                arrayOf("es"),
                arrayOf("fr"),
                arrayOf("pt"),
                arrayOf("hi"),
                arrayOf("zh")
            )
        }
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(locale = localeTag),
        theme = "Theme.TwisterRoulette"
    )

    @Test fun main_idle()           = paparazzi.snapshot { PreviewScene_MainIdle() }
    @Test fun main_result_red()     = paparazzi.snapshot { PreviewScene_MainResult_Red() }
    @Test fun main_result_yellow()  = paparazzi.snapshot { PreviewScene_MainResult_Yellow() }
    @Test fun main_timer_active()   = paparazzi.snapshot { PreviewScene_MainTimer_Active() }
    @Test fun main_voice_listening()= paparazzi.snapshot { PreviewScene_MainVoice_Listening() }
    @Test fun modes_classic()       = paparazzi.snapshot { PreviewScene_Modes_Classic() }
    @Test fun settings()            = paparazzi.snapshot { PreviewScene_Settings() }
}
