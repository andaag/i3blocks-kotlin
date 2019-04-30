package com.neuron.i3blocks.backlight

import com.neuron.i3blocks.BLOCKBUTTON
import com.neuron.i3blocks.FailedToWriteToFileException
import com.neuron.i3blocks.I3BlocksImpl
import com.neuron.i3blocks.Logger
import com.neuron.i3blocks.Posix
import com.neuron.i3blocks.PosixImpl
import platform.posix.exit
import kotlin.math.roundToInt

private val logger = Logger()
private const val BASE_PATH = "/sys/class/backlight/intel_backlight"

class BrightnessApp(private val io: Posix) {
  private val maxBrightness = readBrightnessState("max_brightness").toDouble()
  private val currentBrightness = readBrightnessState("brightness").toDouble()

  private fun readBrightnessState(type: String): Int {
    return io.readFileContents("$BASE_PATH/$type")?.trim()?.toInt()
      ?: throw IllegalStateException("File $BASE_PATH/$type is empty")
  }

  fun increaseBrightness() {
    val newBrightness = currentBrightness + (maxBrightness * 0.05)
    updateBrightness(newBrightness)
  }

  fun decreaseBrightness() {
    val newBrightness = currentBrightness - (maxBrightness * 0.05)
    updateBrightness(newBrightness)
  }

  private fun updateBrightness(brightness: Double) {
    val newBrightness = when {
      brightness > maxBrightness -> maxBrightness.toInt()
      brightness <= 0 -> 0
      else -> brightness.toInt()
    }
    try {
      io.writeFileContents("$BASE_PATH/brightness", "$newBrightness")
    } catch (e: FailedToWriteToFileException) {
      printCantWriteError()
    }
  }

  fun getCurrentBrightness(): Int {
    return ((currentBrightness / maxBrightness) * 100).roundToInt()
  }
}

private fun printCantWriteError() {
  println("""
# Failed to write to $BASE_PATH/brightness. This can be due to permissions.
# Add this to /etc/udev/rules.d/backlight.rules
ACTION=="add", SUBSYSTEM=="backlight", KERNEL=="intel_backlight", RUN+="/bin/chgrp video /sys/class/backlight/%k/brightness"
ACTION=="add", SUBSYSTEM=="backlight", KERNEL=="intel_backlight", RUN+="/bin/chmod g+w /sys/class/backlight/%k/brightness"
  """.trimIndent()
  )
}

fun main(args: Array<String>) {
  val i3Blocks = I3BlocksImpl()
  val io = PosixImpl()

  logger.debug("Trying to access $BASE_PATH")
  if (!io.exist(BASE_PATH)) {
    logger.debug("Can't find brightness file, exiting without info.")
    exit(0)
  }
  if (!io.canWrite("$BASE_PATH/brightness")) {
    printCantWriteError()
    exit(0)
  }

  val app = BrightnessApp(io)

  val blockButton = i3Blocks.getBlockButton()
  logger.debug("Block button $blockButton")
  when (blockButton) {
    BLOCKBUTTON.SCROLL_UP -> app.increaseBrightness()
    BLOCKBUTTON.SCROLL_DOWN -> app.decreaseBrightness()
    else -> {
      //no-op
    }
  }

  val arg = args.firstOrNull()?.trim()
  logger.debug("Argument is $arg")
  when (arg) {
    "up" -> app.increaseBrightness()
    "down" -> app.decreaseBrightness()
    else -> {
      //no-op
    }
  }

  val brightnessPercentage = app.getCurrentBrightness()
  println("$brightnessPercentage%")
}

