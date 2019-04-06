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
