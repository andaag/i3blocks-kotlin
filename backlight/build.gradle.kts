plugins {
    id("kotlin-multiplatform") version "1.3.21"
}

kotlin {
    linuxX64("linux") {
        //compilations["main"].outputKinds("executable")
        binaries {
            executable("backlight") {
                entryPoint = "com.neuron.i3blocks.backlight.main"
            }
        }
    }
    /*binaries {
        executable("test", listOf(RELEASE)) {
            // Build a binary on the basis of the test compilation.
            compilation = compilations["main"]
        }
    }*/

    targets.all {

    }
}


/*binaries {
    framework {

    }
}*/
/*task runProgram {
    def buildType = 'RELEASE' // Change to 'DEBUG' to run application with debug symbols.
    dependsOn kotlin.targets.linux.compilations.main.linkTaskName('EXECUTABLE', buildType)
    doLast {
        def programFile = kotlin.targets.linux.compilations.main.getBinary('EXECUTABLE', buildType)
        exec {
            executable programFile
            args ''
        }
    }
}
*/
