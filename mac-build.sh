#!/bin/bash
rm -rf ../turbo-build/out-mac

java -jar ../turbo-build/packr-all-4.0.0.jar \
     --platform mac \
     --jdk ../turbo-build/mac.tar.gz \
     --useZgcIfSupportedOs \
     --executable FernansGrace \
     --classpath ./lwjgl3/build/lib/FernansGrace-1.0.0.jar \
     --mainclass io.github.grace.ni.fernan.lwjgl3.Lwjgl3Laucher \
     --vmargs Xmx1G XstartOnFirstThread \
     --resources assets/* \
     --output ../turbo-build/out-mac

butler push ../turbo-build/out-mac sweatshirtfoo/FernansGrace:mac
