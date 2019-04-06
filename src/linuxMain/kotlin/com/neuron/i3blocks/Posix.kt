package com.neuron.i3blocks

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.F_OK
import platform.posix.R_OK
import platform.posix.W_OK
import platform.posix.access
import platform.posix.closedir
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.lstat
import platform.posix.opendir
import platform.posix.readdir

interface Posix {
  fun readFileContents(filename: String, bufferLength: Int = 64 * 1024): String?
  fun canAccessPath(filename: String): Boolean
  fun writeFileContents(filename: String, content: String)
  fun exist(filename: String) = access(filename, F_OK) != -1
  fun canWrite(filename: String) = access(filename, W_OK) != -1
  fun canRead(filename: String) = access(filename, R_OK) != -1
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

  fun listDirectory(path: String): Sequence<String> = sequence {
    val dir = opendir(path)
    try {
      var next = readdir(dir)
      while (next != null) {
        // should call lstat on these
        yield(next.pointed.d_name.toKString())
        next = readdir(dir)
      }
    } finally {
      closedir(dir)
    }
  }

  override fun canAccessPath(filename: String): Boolean = access(filename, F_OK) != -1
}

class FailedToReadFileException(filename: String) : IllegalStateException("Can't open file $filename for reading")
class FailedToWriteToFileException(filename: String) : IllegalStateException("Can't open file $filename for writing")
