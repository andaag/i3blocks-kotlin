package com.neuron.i3blocks

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.F_OK
import platform.posix.access
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import kotlin.math.roundToInt

private const val BASE_PATH = "/home/neuron/intel_backlight_sample"

private fun readFileContents(filename: String, bufferLength: Int = 64 * 1024): String? {
  val file = fopen(filename, "r")
    ?: throw IllegalStateException("Can't open file $filename")
  try {
    memScoped {
      val buffer = allocArray<ByteVar>(bufferLength)
      return fgets(buffer, bufferLength, file)?.toKString()
    }
  } finally {
    fclose(file)
  }
}

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
    println("Can't access $BASE_PATH")
  }
}
