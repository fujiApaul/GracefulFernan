#!/bin/bash
rm -rf ../turbo-build/out-win

java -jar ../turbo-build/packr-all-4.0.0.jar \
     --platform windows64 \
     --jdk ../turbo-build/windows.zip \
     --useZgcIfSupportedOs \
     --executable FernansGrace \
     --classpath ./lwjgl3/build/lib/FernansGrace-1.0.0.jar \
     --mainclass io.github.grace.ni.fernan.lwjgl3.Lwjgl3Laucher \
     --vmargs Xmx1G XstartOnFirstThread \
     --resources assets/* \
     --output ../turbo-build/out-win

butler push ../turbo-build/out-win sweatshirtfoo/FernansGrace:win

