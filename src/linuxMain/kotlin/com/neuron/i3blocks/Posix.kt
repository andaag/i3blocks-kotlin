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
import platform.posix.pclose
import platform.posix.popen
import platform.posix.readdir

interface Posix {
  fun readFileContents(filename: String, bufferLength: Int = 64 * 1024): String?
  fun canAccessPath(filename: String): Boolean
  fun writeFileContents(filename: String, content: String)
  fun listDirectory(path: String): Sequence<String>
  fun execute(cmd:String, bufferLength: Int = 512 * 1024):String

  fun exist(filename: String) = access(filename, F_OK) != -1
  fun canWrite(filename: String) = access(filename, W_OK) != -1
  fun canRead(filename: String) = access(filename, R_OK) != -1
}

class PosixImpl : Posix {
  override fun execute(cmd: String, bufferLength: Int): String {
    val file = popen(cmd, "r")
    return try {
      memScoped {
        val buffer = allocArray<ByteVar>(bufferLength)
        buildString {
          var line = fgets(buffer, bufferLength, file)?.toKString()
          while (line != null) {
            append(line)
            line = fgets(buffer, bufferLength, file)?.toKString()
          }
        }
      }
    } finally {
      val exitStatus = pclose(file)
      if (exitStatus != 0) {
        throw CommandExitedWithErrorCode(cmd, exitStatus)
      }
    }
  }

  override fun readFileContents(filename: String, bufferLength: Int): String? {
    val file = fopen(filename, "r")
      ?: throw FailedToReadFileException(filename)
    return try {
      memScoped {
        val buffer = allocArray<ByteVar>(bufferLength)
        buildString {
          var line = fgets(buffer, bufferLength, file)?.toKString()
          while (line != null) {
            append(line)
            line = fgets(buffer, bufferLength, file)?.toKString()
          }
        }

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

  override fun listDirectory(path: String): Sequence<String> = sequence {
    val dir = opendir(path)
    try {
      var next = readdir(dir)
      while (next != null) {
        // should call lstat on these
        val name = next.pointed.d_name.toKString()
        if (name != "." && name != "..") {
          yield(name)
        }
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
class CommandExitedWithErrorCode(command:String, errorCode:Int) : IllegalStateException("Command '$command' exited with $errorCode")
