package com.neuron.i3blocks.network

import com.neuron.i3blocks.I3Blocks
import com.neuron.i3blocks.I3BlocksImpl
import com.neuron.i3blocks.Posix
import com.neuron.i3blocks.PosixImpl
import platform.posix.sleep

const val PROC_DEV = "/proc/net/dev"
//const val PROC_WIFI_DEV = "/proc/net/wireless"
const val MIN_RXTX = 100 * 1024L
const val SLEEP_INTERVAL = 5

class NetworkApp(private val io: Posix, private val i3Blocks: I3Blocks) {

  fun prettyPrint(): String {
    val initialDevices = parseDevices().toList()
    sleep(SLEEP_INTERVAL.toUInt())
    val afterSleep = parseDevices().toList().groupBy { it.deviceName }

    val deviceInfo = initialDevices.joinToString { dev ->
      buildString {
        val rxBytes = ((afterSleep[dev.deviceName]?.let {
          it[0].recieveBytes
        } ?: -1) - dev.recieveBytes) / SLEEP_INTERVAL
        val txBytes = ((afterSleep[dev.deviceName]?.let {
          it[0].transmitBytes
        } ?: -1) - dev.transmitBytes) / SLEEP_INTERVAL

        val state = getDeviceState(dev.deviceName)
        if (state != "down") {
          append(i3Blocks.spanColor(dev.deviceName, "green"))
          if (rxBytes > MIN_RXTX) {
            append(" rx ${rxBytes / 1024} kB/s")
          }
          if (txBytes > MIN_RXTX) {
            append(" tx ${txBytes / 1024} kB/s")
          }
        } else {
          append(i3Blocks.spanColor(dev.deviceName, "red"))
        }
      }
    }

    return if (deviceInfo.isEmpty()) {
      i3Blocks.spanColor("none", "red")
    } else {
      deviceInfo
    }
  }

  fun parseRouteInfo(): Sequence<RouteInfo> {
    val routeInfo = io.execute("route -n")

    return sequence {
      routeInfo.split("\n").forEachIndexed { i, line ->
        if (i >= 2) {
          val split = line.split(" ")
              .map { it.trim() }
              .filter { it.isNotEmpty() }
          yield(RouteInfo(split[7], split[3], split[0]))
        }
      }
    }
  }

  private fun getDeviceState(deviceName: String) =
      io.readFileContents("/sys/class/net/$deviceName/operstate")?.trim()
        ?: throw IllegalStateException("Failed to get operstate for $deviceName")

  fun parseDevices(): Sequence<NetworkDevice> {
    val dev = io.readFileContents(PROC_DEV)
      ?: throw IllegalStateException("Failed to read $PROC_DEV")

    // Other things to harvest:
    // speed = 1000 (only on physical hardware)
    // operstate = up

    // route info.. need for vpn.

    return sequence {
      dev.split("\n").forEach { line ->
        val entries = line.split(" ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (entries.isNotEmpty()) {
          val name = entries[0]
          val isDocker = name.startsWith("docker")
          val isLoopback = name == "lo:"
          if (name.endsWith(":") && !isDocker && !isLoopback) {
            yield(
                NetworkDevice(
                    deviceName = name.trim(':'),
                    recieveBytes = entries[1].trim().toLong(),
                    transmitBytes = entries[9].trim().toLong()
                )
            )
          }
        }
      }
    }
  }
}

fun main(args: Array<String>) {
  val i3Blocks = I3BlocksImpl()
  val io = PosixImpl()

  val net = NetworkApp(io, i3Blocks)
  println(net.prettyPrint())
}

data class NetworkDevice(
  val deviceName: String,
  val recieveBytes: Long,
  val transmitBytes: Long
)

data class RouteInfo(
  val deviceName: String,
  val flags: String,
  val destination: String
)
