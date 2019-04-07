package com.neuron.i3blocks

class IOTest(
  private val readFile: ((filename: String) -> String?)? = null,
  private val listDirectory: ((filename: String) -> Sequence<String>)? = null,
  private val execute: ((cmd: String) -> String)? = null
) : Posix {
  override fun execute(cmd: String, bufferLength: Int): String {
    return execute?.invoke(cmd) ?: ""
  }

  override fun listDirectory(path: String): Sequence<String> {
    return listDirectory?.invoke(path) ?: emptySequence()
  }

  val writes = mutableListOf<Pair<String, String>>()

  override fun readFileContents(filename: String, bufferLength: Int): String? {
    return readFile?.invoke(filename)
  }

  override fun canAccessPath(filename: String): Boolean = true

  override fun writeFileContents(filename: String, content: String) {
    writes.add(Pair(filename, content))
  }
}
