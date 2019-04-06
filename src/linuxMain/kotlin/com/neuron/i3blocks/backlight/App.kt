package com.neuron.i3blocks.backlight

import com.neuron.i3blocks.BLOCKBUTTON
import com.neuron.i3blocks.FailedToWriteToFileException
import com.neuron.i3blocks.I3BlocksImpl
import com.neuron.i3blocks.Posix
import com.neuron.i3blocks.PosixImpl
import kotlin.math.roundToInt

private const val BASE_PATH = "/home/neuron/intel_backlight_sample"

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
      println("Failed to write to $BASE_PATH/brightness. This can be due to permissions.\n" +
          "Please execute:\n" +
          "#usermod -a -G video YOUR_USER\n" +
          "And create /etc/udev/rules.d/backlight.rules\n" +
          "#ACTION==\"add\", SUBSYSTEM==\"backlight\", KERNEL==\"acpi_video0\", RUN+=\"/bin/chgrp video /sys/class/backlight/%k/brightness\"\n" +
          "#ACTION==\"add\", SUBSYSTEM==\"backlight\", KERNEL==\"acpi_video0\", RUN+=\"/bin/chmod g+w /sys/class/backlight/%k/brightness\"\n")
    }
  }

  fun getCurrentBrightness(): Int {
    return ((currentBrightness / maxBrightness) * 100).roundToInt()
  }
}

fun main() {
  val i3Blocks = I3BlocksImpl()
  val io = PosixImpl()
  val app = BrightnessApp(io)

  val blockButton = i3Blocks.getBlockButton()
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

  if (io.canAccessPath(BASE_PATH)) {
    val brightnessPercentage = app.getCurrentBrightness()
    println("$brightnessPercentage%")
  } else {
    println("")
  }
}

