#!/bin/sh
set -eu

# Build the same WASM-GC client bundle used by GitHub Actions.
# Vercel's build environment provides Java; the project itself requires Java 17+.

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
