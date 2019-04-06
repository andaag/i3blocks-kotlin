plugins {
    id("kotlin-multiplatform") version "1.3.21"
}

kotlin {
    linuxX64("linux") {
        binaries {
            executable("backlight") {
                entryPoint = "com.neuron.i3blocks.backlight.main"
            }
        }
    }
}