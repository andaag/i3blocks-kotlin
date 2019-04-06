package com.neuron.i3blocks.backlight

import com.neuron.i3blocks.IO
import com.neuron.i3blocks.IOImpl
import kotlin.math.roundToInt

private const val BASE_PATH = "/home/neuron/intel_backlight_sample"


class BrightnessApp(private val io: IO) {
  private fun readBrightnessState(type: String): Int {
    return io.readFileContents("$BASE_PATH/$type")?.trim()?.toInt()
      ?: throw IllegalStateException("File $BASE_PATH/$type is empty")
  }

  fun getCurrentBrightness(): Int {
    val maxBrightness = readBrightnessState("max_brightness").toDouble()
    val currentBrightness = readBrightnessState("brightness").toDouble()
    return ((currentBrightness / maxBrightness) * 100).roundToInt()
  }
}

fun main() {
  val io = IOImpl()
  val app = BrightnessApp(io)
  if (io.canAccessPath(BASE_PATH)) {
    val brightnessPercentage = app.getCurrentBrightness()
    println("$brightnessPercentage%")
  } else {
    println("")
  }
}

