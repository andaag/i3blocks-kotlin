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
import platform.posix.fputs

interface Posix {
  fun readFileContents(filename: String, bufferLength: Int = 64 * 1024): String?
  fun canAccessPath(filename: String): Boolean
  fun writeFileContents(filename: String, content: String)
}

class PosixImpl : Posix {
  override fun readFileContents(filename: String, bufferLength: Int): String? {
    val file = fopen(filename, "r")
      ?: throw FailedToReadFileException(filename)
    try {
      memScoped {
        val buffer = allocArray<ByteVar>(bufferLength)
        return fgets(buffer, bufferLength, file)?.toKString()
      }
    } finally {
      fclose(file)
    }
  }

  override fun writeFileContents(filename: String, content: String) {
    val file = fopen(filename, "w")
      ?: throw FailedToWriteToFileException(filename)
    try {
      fputs(content, file)
    } finally {
      fclose(file)
    }
  }

  override fun canAccessPath(filename: String): Boolean {
    return access(filename, F_OK) != -1
  }
}

class FailedToReadFileException(filename:String):IllegalStateException("Can't open file $filename for reading")
class FailedToWriteToFileException(filename:String):IllegalStateException("Can't open file $filename for writing")
