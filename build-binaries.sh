#!/usr/bin/env bash
#
# build-binaries.sh — Download or cross-compile adb and fastboot for Android.
#
# Usage:
#   ./build-binaries.sh              # Download from Termux packages (default)
#   ./build-binaries.sh --ndk        # Cross-compile using Android NDK
#   ./build-binaries.sh --help       # Show this help
#
set -euo pipefail

ASSETS_DIR="app/src/main/assets/bin"
CHECKSUMS_FILE="app/src/main/assets/checksums.sha256"
TERMUX_PACKAGES_BASE="https://packages.termux.dev/apt/termux-main"
TERMUX_PACKAGES_INDEX="${TERMUX_PACKAGES_BASE}/dists/stable/main"

ABIS=("arm64-v8a" "armeabi-v7a" "x86_64")
TERMUX_ARCH_NAMES=("aarch64" "arm" "x86_64")

show_help() {
    cat <<'EOF'
Pontifex Binary Builder
=======================

Download or cross-compile adb and fastboot for Android devices.

Methods:
  (default)       Download pre-built static binaries from Termux packages.
                  These are compiled for Android and run natively on ARM/x86
                  devices without root.

  --ndk           Cross-compile using the Android NDK. Requires ANDROID_NDK_HOME
                  or NDK_PATH environment variable. Clones the standalone
                  android-tools repo and builds via CMake.

  --help, -h      Show this help message.

Output:
  Binaries are placed in app/src/main/assets/bin/<abi>/
  Checksums are written to app/src/main/assets/checksums.sha256
EOF
}

log_info()  { echo -e "\033[1;34m[INFO]\033[0m  $*"; }
log_ok()    { echo -e "\033[1;32m[OK]\033[0m    $*"; }
log_warn()  { echo -e "\033[1;33m[WARN]\033[0m  $*"; }
log_error() { echo -e "\033[1;31m[ERROR]\033[0m $*"; }

# ─── Primary method: Download from Termux packages ───────────────────────────

get_latest_version() {
    local arch="$1"
    local index_url="${TERMUX_PACKAGES_INDEX}/binary-${arch}/Packages"
    local version
    version=$(curl -sL "$index_url" 2>/dev/null | grep -m1 '^Package: android-tools$' -A2 | grep '^Version:' | awk '{print $2}' | tr -d '[:space:]')
    echo "$version"
}

download_deb() {
    local arch="$1"
    local version="$2"
    local deb_url="${TERMUX_PACKAGES_BASE}/pool/main/a/android-tools/android-tools_${version}_${arch}.deb"
    local tmp_dir="$3"

    mkdir -p "$tmp_dir"
    log_info "Downloading android-tools ${version} for ${arch}..."
    if ! curl -sL -o "${tmp_dir}/android-tools.deb" "$deb_url"; then
        log_error "Failed to download .deb for ${arch}"
        return 1
    fi
    log_ok "Downloaded ${arch} .deb"
}

extract_binaries_from_deb() {
    local deb_file="$1"
    local output_dir="$2"
    local tmp_extract
    tmp_extract=$(mktemp -d)

    # Extract .deb (it's an ar archive)
    cd "$tmp_extract"
    ar x "$deb_file" 2>/dev/null || { cd - > /dev/null; rm -rf "$tmp_extract"; return 1; }

    # Extract data.tar.*
    local data_tar
    data_tar=$(ls data.tar.* 2>/dev/null | head -1)
    if [ -z "$data_tar" ]; then
        cd - > /dev/null
        rm -rf "$tmp_extract"
        return 1
    fi

    tar xf "$data_tar" 2>/dev/null
    cd - > /dev/null

    # Locate binaries
    local adb_bin fastboot_bin
    adb_bin=$(find "$tmp_extract" -name "adb" -type f -executable 2>/dev/null | head -1)
    fastboot_bin=$(find "$tmp_extract" -name "fastboot" -type f -executable 2>/dev/null | head -1)

    # Fallback: try non-executable files
    if [ -z "$adb_bin" ]; then
        adb_bin=$(find "$tmp_extract" -path "*/usr/bin/adb" -type f 2>/dev/null | head -1)
    fi
    if [ -z "$fastboot_bin" ]; then
        fastboot_bin=$(find "$tmp_extract" -path "*/usr/bin/fastboot" -type f 2>/dev/null | head -1)
    fi

    mkdir -p "$output_dir"

    if [ -n "$adb_bin" ]; then
        cp "$adb_bin" "$output_dir/adb"
        chmod 755 "$output_dir/adb"
    else
        log_warn "adb not found in .deb for extraction"
        rm -rf "$tmp_extract"
        return 1
    fi

    if [ -n "$fastboot_bin" ]; then
        cp "$fastboot_bin" "$output_dir/fastboot"
        chmod 755 "$output_dir/fastboot"
    else
        log_warn "fastboot not found in .deb, creating placeholder"
        cat > "$output_dir/fastboot" <<'PLACEHOLDER'
#!/system/bin/sh
echo "fastboot: placeholder - rebuild with --ndk or provide real binary"
exit 1
PLACEHOLDER
        chmod 755 "$output_dir/fastboot"
    fi

    rm -rf "$tmp_extract"
    return 0
}

download_from_termux() {
    log_info "=== Downloading binaries from Termux packages ==="

    for i in "${!ABIS[@]}"; do
        local abi="${ABIS[$i]}"
        local arch="${TERMUX_ARCH_NAMES[$i]}"
        local output_dir="${ASSETS_DIR}/${abi}"
        local tmp_dir
        tmp_dir=$(mktemp -d)

        log_info "Processing ${abi} (termux arch: ${arch})..."

        local version
        version=$(get_latest_version "$arch")
        if [ -z "$version" ]; then
            log_warn "Could not determine latest version for ${arch}, trying fallback..."
            version="28.0.2-1"
        fi
        log_info "Version for ${arch}: ${version}"

        if download_deb "$arch" "$version" "$tmp_dir"; then
            if extract_binaries_from_deb "${tmp_dir}/android-tools.deb" "$output_dir"; then
                log_ok "${abi}: adb and fastboot extracted"
            else
                log_error "${abi}: Failed to extract binaries from .deb"
            fi
        else
            log_error "${abi}: Failed to download .deb"
        fi

        rm -rf "$tmp_dir"
    done
}

# ─── Secondary method: Cross-compile with NDK ───────────────────────────────

compile_with_ndk() {
    local ndk_path="${ANDROID_NDK_HOME:-${NDK_PATH:-}}"
    if [ -z "$ndk_path" ]; then
        log_error "ANDROID_NDK_HOME or NDK_PATH must be set for --ndk mode"
        exit 1
    fi

    if [ ! -d "$ndk_path" ]; then
        log_error "NDK path does not exist: $ndk_path"
        exit 1
    fi

    log_info "=== Cross-compiling with NDK at ${ndk_path} ==="

    local work_dir
    work_dir=$(mktemp -d)
    local src_dir="${work_dir}/android-tools"

    log_info "Cloning android-tools..."
    git clone --depth=1 https://github.com/nmeum/android-tools "$src_dir"

    for i in "${!ABIS[@]}"; do
        local abi="${ABIS[$i]}"
        local output_dir="${ASSETS_DIR}/${abi}"
        local build_dir="${src_dir}/build-${abi}"

        log_info "Building for ${abi}..."

        mkdir -p "$build_dir"
        cd "$build_dir"

        # Map ABI to NDK toolchain triple
        local triple
        case "$abi" in
            arm64-v8a)   triple="aarch64-linux-android" ;;
            armeabi-v7a) triple="armv7a-linux-androideabi" ;;
            x86_64)      triple="x86_64-linux-android" ;;
        esac

        cmake \
            -DCMAKE_TOOLCHAIN_FILE="${ndk_path}/build/cmake/android.toolchain.cmake" \
            -DANDROID_ABI="$abi" \
            -DANDROID_PLATFORM="android-26" \
            -DCMAKE_BUILD_TYPE=Release \
            "$src_dir" 2>/dev/null

        cmake --build . --config Release -j"$(nproc)" 2>/dev/null

        mkdir -p "$output_dir"

        # Find and copy built binaries
        local adb_bin fastboot_bin
        adb_bin=$(find "$build_dir" -name "adb" -type f ! -name "*.o" ! -name "*.d" 2>/dev/null | head -1)
        fastboot_bin=$(find "$build_dir" -name "fastboot" -type f ! -name "*.o" ! -name "*.d" 2>/dev/null | head -1)

        if [ -n "$adb_bin" ]; then
            cp "$adb_bin" "$output_dir/adb"
            chmod 755 "$output_dir/adb"
            log_ok "${abi}: adb built"
        else
            log_warn "${abi}: adb not found in build output"
        fi

        if [ -n "$fastboot_bin" ]; then
            cp "$fastboot_bin" "$output_dir/fastboot"
            chmod 755 "$output_dir/fastboot"
            log_ok "${abi}: fastboot built"
        else
            log_warn "${abi}: fastboot not found in build output"
        fi

        cd - > /dev/null
    done

    rm -rf "$work_dir"
}

# ─── Generate checksums ─────────────────────────────────────────────────────

generate_checksums() {
    log_info "Generating SHA-256 checksums..."
    > "$CHECKSUMS_FILE"

    for abi in "${ABIS[@]}"; do
        local dir="${ASSETS_DIR}/${abi}"
        for binary in adb fastboot; do
            local file="${dir}/${binary}"
            if [ -f "$file" ] && [ -s "$file" ]; then
                local hash
                hash=$(sha256sum "$file" | awk '{print $1}')
                echo "${hash}  bin/${abi}/${binary}" >> "$CHECKSUMS_FILE"
                log_ok "${abi}/${binary}: ${hash}"
            else
                log_warn "Skipping ${abi}/${binary} (missing or empty)"
            fi
        done
    done

    log_ok "Checksums written to ${CHECKSUMS_FILE}"
}

# ─── Main ────────────────────────────────────────────────────────────────────

main() {
    local mode="${1:-download}"

    case "$mode" in
        --help|-h)
            show_help
            exit 0
            ;;
        --ndk)
            compile_with_ndk
            ;;
        download|"")
            download_from_termux
            ;;
        *)
            log_error "Unknown mode: $mode"
            show_help
            exit 1
            ;;
    esac

    generate_checksums
    log_ok "=== Binary pipeline complete ==="
}

main "$@"
