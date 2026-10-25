#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-or-later
#
# Gera o APK em app/build/outputs/apk/debug/app-debug.apk
# Precisa de JDK 17 ou 21 e do Android SDK. Se JAVA_HOME / ANDROID_HOME
# não estiverem definidos, procura nos lugares usados pelo guia do README.
set -e
cd "$(dirname "$0")"

if [ -z "$JAVA_HOME" ]; then
    JAVA_HOME="$(ls -d "$HOME"/.local/share/jdks/jdk-21*/ 2>/dev/null | head -1)"
    export JAVA_HOME
fi
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"

./gradlew assembleDebug "$@"
echo
echo "APK pronto: $(pwd)/app/build/outputs/apk/debug/app-debug.apk"
