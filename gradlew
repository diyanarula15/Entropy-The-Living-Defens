#!/usr/bin/env bash
set -e

APP_NAME="$(basename "$0")"
APP_BASE_NAME="${APP_NAME%.*}"
APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

CLASSPATH="$APP_HOME"/gradle/wrapper/gradle-wrapper.jar

exec java \
    -Dorg.gradle.appname="$APP_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
