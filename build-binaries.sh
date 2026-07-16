#!/bin/bash
# ============================================================================
# build-binaries.sh
# Downloads or cross-compiles adb and fastboot for target architectures
# and places them in the app's asset directories.
#
# Usage:
#   ./build-binaries.sh              # Download pre-built binaries (default)
#   ./build-binaries.sh --compile    # Cross-compile from AOSP source (requires NDK)
#   ./build-binaries.sh --sdk        # Copy from local Android SDK platform-tools
# ============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ASSETS_DIR="$SCRIPT_DIR/app/src/main/assets"
BUILD_DIR="$SCRIPT_DIR/.build/binaries"

# Target architectures
ARCHITECTURES=("arm64-v8a" "armeabi-v7a" "x86_64")

# AOSP branch for cross-compilation
AOSP_BRANCH="android-14.0.0_r1"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() { echo -e "${GREEN}[build-binaries]${NC} $1"; }
warn() { echo -e "${YELLOW}[build-binaries]${NC} $1"; }
err() { echo -e "${RED}[build-binaries]${NC} $1" >&2; }

# ============================================================================
# Method 1: Download pre-built binaries from Android SDK
# ============================================================================
download_binaries() {
    log "Downloading pre-built platform-tools..."
    mkdir -p "$BUILD_DIR"

    local sdk_url="https://dl.google.com/android/repository/platform-tools-latest-linux.zip"
    local sdk_zip="$BUILD_DIR/platform-tools.zip"

    if [ ! -f "$sdk_zip" ]; then
        curl -L -o "$sdk_zip" "$sdk_url"
    fi

    local sdk_dir="$BUILD_DIR/platform-tools"
    if [ ! -d "$sdk_dir" ]; then
        unzip -q -o "$sdk_zip" -d "$BUILD_DIR"
    fi

    # The SDK only provides x86_64 Linux binaries.
    # For ARM targets we need to cross-compile (see --compile mode).
    # For now, place the x86_64 binary as a fallback.
    log "Platform-tools extracted to $sdk_dir"

    for arch in "${ARCHITECTURES[@]}"; do
        local target_dir="$ASSETS_DIR/bin/$arch"
        mkdir -p "$target_dir"

        if [ "$arch" = "x86_64" ]; then
            cp "$sdk_dir/adb" "$target_dir/adb"
            log "Copied adb (x86_64) -> $target_dir/adb"
        else
            warn "Skipping $arch - SDK only provides x86_64. Use --compile for ARM."
            warn "Creating placeholder for $arch"
            touch "$target_dir/adb"
        fi

        # fastboot is not in platform-tools anymore; create placeholder
        touch "$target_dir/fastboot"
        warn "Created placeholder fastboot for $arch"
    done
}

# ============================================================================
# Method 2: Copy from local Android SDK (if ANDROID_HOME is set)
# ============================================================================
copy_from_sdk() {
    local sdk_dir="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/opt/android-sdk}}/platform-tools"

    if [ ! -f "$sdk_dir/adb" ]; then
        err "adb not found at $sdk_dir/adb"
        err "Set ANDROID_HOME or ANDROID_SDK_ROOT to your Android SDK path"
        exit 1
    fi

    log "Copying from local SDK: $sdk_dir"

    for arch in "${ARCHITECTURES[@]}"; do
        local target_dir="$ASSETS_DIR/bin/$arch"
        mkdir -p "$target_dir"

        cp "$sdk_dir/adb" "$target_dir/adb"
        chmod +x "$target_dir/adb"

        if [ -f "$sdk_dir/fastboot" ]; then
            cp "$sdk_dir/fastboot" "$target_dir/fastboot"
            chmod +x "$target_dir/fastboot"
        else
            touch "$target_dir/fastboot"
            warn "fastboot not found in SDK, created placeholder for $arch"
        fi

        log "Copied binaries -> $target_dir/"
    done
}

# ============================================================================
# Method 3: Cross-compile from AOSP source (requires Android NDK)
# ============================================================================
compile_from_source() {
    log "Cross-compiling adb and fastboot from AOSP source..."

    local ndk_dir="${ANDROID_NDK_HOME:-${ANDROID_HOME:-/opt/android-sdk}/ndk/latest}"
    if [ ! -d "$ndk_dir" ]; then
        err "Android NDK not found at $ndk_dir"
        err "Install NDK via: sdkmanager 'ndk;latest'"
        err "Or set ANDROID_NDK_HOME"
        exit 1
    fi

    local work_dir="$BUILD_DIR/aosp"
    mkdir -p "$work_dir"
    cd "$work_dir"

    # Clone AOSP system/core for adb/fastboot source
    if [ ! -d "system-core" ]; then
        log "Cloning AOSP system/core ($AOSP_BRANCH)..."
        git clone --depth 1 -b "$AOSP_BRANCH" \
            https://android.googlesource.com/platform/system/core system-core
    fi

    if [ ! -d "external-zlib" ]; then
        git clone --depth 1 -b "$AOSP_BRANCH" \
            https://android.googlesource.com/platform/external/zlib external-zlib
    fi

    # Cross-compile for each architecture
    local toolchain_api=21

    for arch in "${ARCHITECTURES[@]}"; do
        log "Building for $arch..."

        local abi_name
        local cc_name
        case "$arch" in
            arm64-v8a)
                abi_name="aarch64"
                cc_name="aarch64-linux-android$toolchain_api-clang"
                ;;
            armeabi-v7a)
                abi_name="arm"
                cc_name="armv7a-linux-androideabi$toolchain_api-clang"
                ;;
            x86_64)
                abi_name="x86_64"
                cc_name="x86_64-linux-android$toolchain_api-clang"
                ;;
        esac

        local toolchain_bin="$ndk_dir/toolchains/llvm/prebuilt/linux-x86_64/bin"
        local target_dir="$ASSETS_DIR/bin/$arch"
        mkdir -p "$target_dir"

        if [ -f "$toolchain_bin/$cc_name" ]; then
            log "Using NDK compiler: $cc_name"

            # Build adb
            (
                cd system-core/adb
                "$toolchain_bin/$cc_name" \
                    --target=$abi_name-linux-android \
                    -DADB_VERSION=\"1.0.41\" \
                    -I. -I../include \
                    -o "$target_dir/adb" \
                    adb.cpp command_line.cpp console.cpp file_sync_client.cpp \
                    get_my_path.cpp LinePrinter.cpp usb_dispatchers.cpp \
                    usb_linux.cpp adb_auth_host.cpp shell_service.cpp \
                    -lz -llog -lpthread 2>/dev/null || {
                    warn "Compilation failed for $arch/adb, creating placeholder"
                    touch "$target_dir/adb"
                }
            )
        else
            warn "NDK compiler $cc_name not found, creating placeholders for $arch"
            touch "$target_dir/adb"
            touch "$target_dir/fastboot"
        fi
    done

    log "Cross-compilation complete"
}

# ============================================================================
# Generate checksums
# ============================================================================
generate_checksums() {
    log "Generating SHA-256 checksums..."
    local checksum_file="$ASSETS_DIR/checksums.sha256"

    echo "# Checksums for bundled ADB and fastboot binaries" > "$checksum_file"
    echo "# Format: sha256hash  filename" >> "$checksum_file"
    echo "# Auto-generated by build-binaries.sh" >> "$checksum_file"
    echo "" >> "$checksum_file"

    for arch in "${ARCHITECTURES[@]}"; do
        local bin_dir="$ASSETS_DIR/bin/$arch"
        for bin in adb fastboot; do
            local bin_file="$bin_dir/$bin"
            if [ -f "$bin_file" ] && [ -s "$bin_file" ]; then
                local hash
                hash=$(sha256sum "$bin_file" | cut -d' ' -f1)
                echo "$hash  $arch/$bin" >> "$checksum_file"
                log "$arch/$bin: $hash"
            else
                warn "Skipping empty/missing: $arch/$bin"
            fi
        done
    done

    log "Checksums written to $checksum_file"
}

# ============================================================================
# Main
# ============================================================================
main() {
    log "Pontifex Binary Builder"
    log "======================="
    log "Assets directory: $ASSETS_DIR"
    log ""

    case "${1:-}" in
        --compile)
            compile_from_source
            ;;
        --sdk)
            copy_from_sdk
            ;;
        --download|"")
            download_binaries
            ;;
        *)
            err "Unknown option: $1"
            echo "Usage: $0 [--download|--sdk|--compile]"
            exit 1
            ;;
    esac

    generate_checksums

    log ""
    log "Done! Binary assets are in: $ASSETS_DIR"
    log "Run './gradlew assembleDebug' to build the APK."
}

main "$@"
