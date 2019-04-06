package com.neuron.i3blocks

class IOTest(val readRep: (filename: String) -> String?) : Posix {
  val writes = mutableListOf<Pair<String, String>>()

  override fun readFileContents(filename: String, bufferLength: Int): String? {
    return readRep(filename)
  }

  override fun canAccessPath(filename: String): Boolean = true

  override fun writeFileContents(filename: String, content: String) {
    writes.add(Pair(filename, content))
  }
}
