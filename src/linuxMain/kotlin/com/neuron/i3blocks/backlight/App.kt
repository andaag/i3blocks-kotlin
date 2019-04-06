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
      println(
          "Failed to write to $BASE_PATH/brightness. This can be due to permissions.\n" +
              "Please fix:\n" +
              "#usermod -a -G video YOUR_USER\n" +
              "And perform the following on startup\n" +
              "# chgrp video /sys/class/backlight/intel_backlight/brightness && chmod g+w /sys/class/backlight/intel_backlight/brightness"
      )
    }
  }

  fun getCurrentBrightness(): Int {
    return ((currentBrightness / maxBrightness) * 100).roundToInt()
  }
}

fun main() {
  //@todo : support replacing XF86KbdBrightnessDown and XF86KbdBrightnessUp
  //@todo : check permissions on start instead of trying to write first.

  val i3Blocks = I3BlocksImpl()
  val io = PosixImpl()

  logger.debug("Trying to access $BASE_PATH")
  if (!io.canAccessPath(BASE_PATH)) {
    logger.debug("Can't find brightness file, exiting without info.")
    exit(0)
  }

  val app = BrightnessApp(io)

  val blockButton = i3Blocks.getBlockButton()
  logger.debug("Block button $blockButton")
  when (blockButton) {
    BLOCKBUTTON.SCROLL_UP -> {
      app.increaseBrightness()
    }
    BLOCKBUTTON.SCROLL_DOWN -> {
      app.decreaseBrightness()
    }
    else -> {
      //no-op
    }
  }

  val brightnessPercentage = app.getCurrentBrightness()
  println("$brightnessPercentage%")
}

