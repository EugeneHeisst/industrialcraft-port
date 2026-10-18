#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew "$@" build --console=plain
version=$(sed -n "s/^version = '\([^']*\)'/\1/p" build.gradle)
artifact="build/libs/industrialcraft2-modern-$version.jar"
archive="versions/$version"
mkdir -p "$archive"
if [ -f "$archive/$(basename "$artifact")" ] && ! cmp -s "$artifact" "$archive/$(basename "$artifact")"; then
    prior="$archive/previous-$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$prior"
    cp "$archive/$(basename "$artifact")" "$prior/"
fi
cp "$artifact" "$archive/"
sha256sum "$archive/$(basename "$artifact")" > "$archive/SHA256SUMS"

python3 tools/audit_package.py "$artifact" > "$archive/package-audit.json"
