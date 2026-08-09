#!/bin/sh
#
# Gradle start up script for UN*X
#
APP_HOME="$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd -P)"
APP_NAME="Gradle"
APP_BASE_NAME="${0##*/}"
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
MAX_FD="maximum"
warn() { printf '%s\n' "$*"; }
die() { status="$1"; shift; warn "$@"; exit "$status"; }
CLASSPATH="${APP_HOME}/gradle/wrapper/gradle-wrapper.jar"
if ! command -v java >/dev/null 2>&1; then die 1 "ERROR: JAVA_HOME is not set and no 'java' command could be found."; fi
exec java ${DEFAULT_JVM_OPTS} -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
