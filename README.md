# i3 kotlin code
My kotlin native code for i3blocks functionality.


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

# Increase brightness:
build/bin/linux/backlightReleaseExecutable/backlight.kexe up
```

## Using backlight.kexe:
```
# In config:
bindsym XF86KbdBrightnessDown exec "backlight.kexe"
bindsym XF86KbdBrightnessUp exec "backlight.kexe"

# In i3blocks.conf:
[backlight]
label=⛯
command=backlight.kexe
signal=1
interval=10

```

## Notes for self:
- Font awesome copy&pastable icons : https://fontawesome.com/cheatsheet?from=io
- https://fontdrop.info/
- Insert unicode extension for vscode
