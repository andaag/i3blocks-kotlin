package com.neuron.i3blocks.backlight

import com.neuron.i3blocks.readFileContents
import platform.posix.F_OK
import platform.posix.access
import kotlin.math.roundToInt

private const val BASE_PATH = "/home/neuron/intel_backlight_sample"

private fun readBrightnessState(type: String): Int {
  return readFileContents("$BASE_PATH/$type")?.trim()?.toInt()
    ?: throw IllegalStateException("File $BASE_PATH/$type is empty")
}

fun main() {
  if (access(BASE_PATH, F_OK) != -1) {
    val maxBrightness = readBrightnessState("max_brightness").toDouble()
    val currentBrightness = readBrightnessState("brightness").toDouble()
    val brightnessPercentage = ((currentBrightness / maxBrightness) * 100).roundToInt()
    println("$brightnessPercentage%")
  } else {
    println("")
  }
}
