# i3 kotlin code
Native kotlin code for i3blocks functionality.


## To build
```
./gradlew assemble
```

## My Bar config to support icons
```
bar {
        font -misc-fixed-medium-r-normal--13-120-75-75-C-70-iso10646-1
        font pango:DejaVu Sans Mono 10, Awesome 10
        status_command i3blocks -c ~/.i3/i3blocks.conf 
}
```

## Testing:
```
build/bin/linux/backlightReleaseExecutable/backlight.kexe

# Scroll up:
BLOCK_BUTTON=4 build/bin/linux/backlightReleaseExecutable/backlight.kexe
```
