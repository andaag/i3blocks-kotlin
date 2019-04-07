package com.neuron.i3blocks

import com.neuron.i3blocks.backlight.BrightnessApp
import kotlin.test.Test
import kotlin.test.assertEquals

class BacklightTest {
  @Test
  fun `test normal update`() {
    val io = IOTest(readFile = { filename ->
      if (filename.endsWith("max_brightness")) {
        "7500"
      } else {
        "2500"
      }
    })
    val app = BrightnessApp(io)
    app.increaseBrightness()
    assertEquals("2875", io.writes[0].second)
  }

  @Test
  fun `test update above max`() {
    val io = IOTest(readFile = { filename ->
      if (filename.endsWith("max_brightness")) {
        "7500"
      } else {
        "7499"
      }
    })
    val app = BrightnessApp(io)
    app.increaseBrightness()
    assertEquals("7500", io.writes[0].second)
  }
}

