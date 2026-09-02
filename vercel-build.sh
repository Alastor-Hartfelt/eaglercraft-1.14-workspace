#!/bin/sh
set -eu

# Vercel's build image may have a newer Java (for example Java 24), while
# this project explicitly requires a Java 17 toolchain. Download a local
# Temurin 17 JDK so Gradle can satisfy that toolchain requirement.
JDK_DIR=".vercel-jdk-17"
JDK_URL="https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse"

if [ ! -x "$JDK_DIR/bin/java" ]; then
  echo "Java 17 not found; downloading Temurin JDK 17..."
  rm -rf "$JDK_DIR" .vercel-jdk-17.tar.gz
  mkdir -p "$JDK_DIR"
  curl -L --fail --retry 3 "$JDK_URL" -o .vercel-jdk-17.tar.gz
  tar -xzf .vercel-jdk-17.tar.gz --strip-components=1 -C "$JDK_DIR"
  rm -f .vercel-jdk-17.tar.gz
fi

export JAVA_HOME="$(pwd)/$JDK_DIR"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using Java:"
java -version

chmod +x ./gradlew

cd target_teavm_wasm_gc
mkdir -p javascript_dist

# Generate the bootstrap JavaScript required by the WASM-GC client build.
java -jar buildtools/closure-compiler.jar \
  --compilation_level ADVANCED_OPTIMIZATIONS \
  --assume_function_wrapper \
  --emit_use_strict \
  --isolation_mode IIFE \
  --js ../src/wasm-gc-teavm-bootstrap/js/externs.js \
  --js ../src/wasm-gc-teavm-bootstrap/js/main.js \
  --js_output_file javascript_dist/bootstrap.js

cd ..
./gradlew :target_teavm_wasm_gc:makeMainWasmClientBundle --no-daemon

# Vercel needs an index.html at the root of the deployment output.
cp target_teavm_wasm_gc/javascript_dist/Eaglercraft_1.14.4_WASM-GC_Offline_Download.html \
   target_teavm_wasm_gc/javascript_dist/index.html

# Fail the deployment if the playable bundle is incomplete.
test -f target_teavm_wasm_gc/javascript_dist/index.html
test -f target_teavm_wasm_gc/javascript_dist/bootstrap.js
test -f target_teavm_wasm_gc/javascript_dist/assets.epw

echo "Playable Eaglercraft 1.14 WASM-GC bundle created successfully."
