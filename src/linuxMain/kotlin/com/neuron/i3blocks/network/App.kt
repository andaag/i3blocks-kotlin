package com.neuron.i3blocks.network

import com.neuron.i3blocks.I3BlocksImpl
import com.neuron.i3blocks.Logger
import com.neuron.i3blocks.Posix
import com.neuron.i3blocks.PosixImpl

private val logger = Logger()

class NetworkApp(private val io: Posix) {
  fun devices() {

  }
}


fun main(args: Array<String>) {
  val i3Blocks = I3BlocksImpl()
  val io = PosixImpl()

  val net = NetworkApp(io)
  println(net.devices())
}

