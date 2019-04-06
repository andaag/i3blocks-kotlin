package com.neuron.i3blocks

import kotlinx.cinterop.toKString
import platform.posix.getenv

class Logger {
  fun debug(content: String) {
    if (isDebug()) {
      println("Debug : $content")
    }
  }

  private fun isDebug(): Boolean {
    return getenv("DEBUG")?.toKString()?.trim() == "1"
  }
}
