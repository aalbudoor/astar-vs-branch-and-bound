#!/usr/bin/env bash
# Compile the Java sources and regenerate every result, map and figure used in the report.
set -euo pipefail
cd "$(dirname "$0")"

rm -rf out
mkdir -p out
javac -d out src/*.java
java -cp out Experiment .
