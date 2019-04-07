package com.neuron.i3blocks

import com.neuron.i3blocks.network.NetworkApp
import com.neuron.i3blocks.network.NetworkDevice
import com.neuron.i3blocks.network.RouteInfo
import kotlin.native.concurrent.AtomicInt
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkTest {
  companion object {
    val WIFI_DEVICE = """Inter-| sta-|   Quality        |   Discarded packets               | Missed | WE
 face | tus | link level noise |  nwid  crypt   frag  retry   misc | beacon | 22
wlp2s0: 0000   69.  -41.  -256        0      0      0      0     81        0
  """.trimIndent()

    val DEV_DEVICE = """Inter-|   Receive                                                |  Transmit
 face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
  eth0: 77847385431 58023914    0  180    0     0          0    206616 27584643387 32338348    0    0    0     0       0          0
    lo:  504177    1861    0    0    0     0          0         0   504177    1861    0    0    0     0       0          0
docker0:       0       0    0    0    0     0          0         0        0       0    0    0    0     0       0          0
wlp2s0:  899735    1826    0    0    0     0          0         0   528852    1622    0    0    0     0       0          0
  """.trimIndent()

    val DEV_DEVICE_READ2 = """Inter-|   Receive                                                |  Transmit
 face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
  eth0: 78047385431 58023914    0  180    0     0          0    206616 27584643387 32338348    0    0    0     0       0          0
    lo:  504177    1861    0    0    0     0          0         0   504177    1861    0    0    0     0       0          0
docker0:       0       0    0    0    0     0          0         0        0       0    0    0    0     0       0          0
wlp2s0:  899735    1826    0    0    0     0          0         0   528852    1622    0    0    0     0       0          0
  """.trimIndent()
  }

  @Test
  fun `parse network device information`() {
    val io = IOTest(
        readFile = {
          if (it == "/proc/net/dev") {
            DEV_DEVICE
          } else {
            WIFI_DEVICE
          }
        })
    val app = NetworkApp(io, I3BlocksImpl())
    val devices = app.parseDevices().toList()

    assertEquals(
        listOf(
            NetworkDevice("eth0", 77847385431, 27584643387),
            NetworkDevice("wlp2s0", 899735, 528852)
        ),
        devices
    )
  }

  @Test
  fun `prettyprint format`() {
    val readNum = AtomicInt(0)
    val io = IOTest(
        readFile = {
          readNum.increment()
          if (readNum.value == 1) {
            DEV_DEVICE
          } else {
            DEV_DEVICE_READ2
          }
        })
    val app = NetworkApp(io, I3BlocksImpl())
    val output = app.prettyPrint()

    assertEquals(
        "<span color='green'>eth0</span> rx 39062 kB/s, <span color='green'>wlp2s0</span>",
        output
    )
  }

  // todo : Wanted prettyprint output
  // Connections :

  @Test
  fun `parse route`() {
    val io = IOTest(
        execute = {
          """Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
0.0.0.0         192.168.0.1     0.0.0.0         UG    100    0        0 eth0
10.5.0.1        10.7.5.205      255.255.255.255 UGH   0      0        0 tun0
          """.trimIndent()
        })
    val app = NetworkApp(io, I3BlocksImpl())
    val routes = app.parseRouteInfo().toList()

    assertEquals(
        listOf(
            RouteInfo("eth0", "UG", "0.0.0.0"),
            RouteInfo("tun0", "UGH", "10.5.0.1")
        ),
        routes
    )
  }
}
