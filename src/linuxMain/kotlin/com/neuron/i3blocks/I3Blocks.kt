package com.neuron.i3blocks

import kotlinx.cinterop.toKString
import platform.posix.getenv

interface I3Blocks {
  fun getBlockButton(): BLOCKBUTTON
  fun spanColor(c:String, color:String) = "<span color='$color'>$c</span>"
}

class I3BlocksImpl : I3Blocks {
  override fun getBlockButton(): BLOCKBUTTON {
    val blockButton = getenv("BLOCK_BUTTON")?.toKString()?.trim() ?: ""
    return when (blockButton) {
      "1" -> BLOCKBUTTON.LEFT
      "2" -> BLOCKBUTTON.MIDDLE
      "3" -> BLOCKBUTTON.RIGHT
      "4" -> BLOCKBUTTON.SCROLL_UP
      "5" -> BLOCKBUTTON.SCROLL_DOWN
      "" -> BLOCKBUTTON.NO_CLICK
      else -> BLOCKBUTTON.UNKNOWN
    }
  }
}


enum class BLOCKBUTTON {
  LEFT,
  MIDDLE,
  RIGHT,
  SCROLL_UP,
  SCROLL_DOWN,
  UNKNOWN,
  NO_CLICK
}
