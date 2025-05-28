#!/bin/bash
rm -rf ../turbo-build/out-linux

java -jar ../turbo-build/packr-all-4.0.0.jar \
     --platform linux64 \
     --jdk ../turbo-build/linux.tar.gz \
     --useZgcIfSupportedOs \
     --executable FernansGrace \
     --classpath ./lwjgl3/build/lib/FernansGrace-1.0.0.jar \
     --mainclass io.github.grace.ni.fernan.lwjgl3.Lwjgl3Laucher \
     --vmargs Xmx1G XstartOnFirstThread \
     --resources assets/* \
     --output ../turbo-build/out-linux

butler push ../turbo-build/out-linux sweatshirtfoo/FernansGrace:linux
