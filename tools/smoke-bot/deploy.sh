#!/usr/bin/env bash
# deploy.sh — build + restart + run scenarios for NTM port smoke-testing
# Requires: Git Bash on Windows, Java 8, Node.js, gradlew in project root
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
TESTSERVER="C:/Users/rad/cc/testserver"
HELPER_MOD_DIR="$PROJECT_ROOT/tools/testhelper-mod"

# ─── Flags ──────────────────────────────────────────────────────
NO_BUILD=false
NO_RESTART=false
ALSO_HELPER=false
SCENARIOS=()

usage() {
    cat <<EOF
Usage: $0 [options] <scenario> [<scenario> ...]

Options:
  --no-build      Skip gradlew build (reuse existing jar)
  --no-restart    Re-use running server (skip kill+start+deploy)
  --also-helper   Also rebuild + deploy ntm-testhelper jar
  --help, -h      Show this help

Scenarios: pass names without .js suffix.
Examples:
  $0 4.4c.crt 4.4c.toaster 4.4c.computer
  $0 --no-build --no-restart _hello_ntm
  $0 --also-helper 4.4c.crt 4.4c.toaster 4.4c.computer
EOF
    exit "${1:-0}"
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --no-build)    NO_BUILD=true;    shift ;;
        --no-restart)  NO_RESTART=true;  shift ;;
        --also-helper) ALSO_HELPER=true; shift ;;
        --help|-h)     usage 0 ;;
        --*)           echo "ERROR: unknown option: $1"; usage 1 ;;
        *)             SCENARIOS+=("$1"); shift ;;
    esac
done

if [ ${#SCENARIOS[@]} -eq 0 ]; then
    echo "ERROR: no scenarios specified"
    usage 1
fi

log() { echo "[deploy] $(date +%H:%M:%S) $*"; }

# ─── 1. Build NTM jar ───────────────────────────────────────────
if [ "$NO_BUILD" = false ]; then
    log "▶ build NTM jar (gradlew build)"
    cd "$PROJECT_ROOT"
    chmod +x gradlew
    if ! ./gradlew build --quiet; then
        log "✗ BUILD FAILED"
        exit 1
    fi
    log "✓ BUILD SUCCESSFUL"
fi

# Locate NTM main jar (exclude testhelper)
NTM_JAR=$(ls -t "$PROJECT_ROOT/build/libs/"ntm-*.jar 2>/dev/null | grep -v testhelper | head -1 || true)
if [ -z "$NTM_JAR" ]; then
    log "✗ NTM jar not found in build/libs/ — run without --no-build first"
    exit 1
fi
log "  NTM jar: $(basename "$NTM_JAR")"

# ─── 2. Build testhelper-mod (optional) ─────────────────────────
HELPER_JAR=""
if [ "$ALSO_HELPER" = true ]; then
    log "▶ build testhelper-mod"
    cd "$HELPER_MOD_DIR"
    chmod +x gradlew
    if ! ./gradlew build --quiet; then
        log "✗ HELPER BUILD FAILED"
        exit 1
    fi
    HELPER_JAR=$(ls -t "$HELPER_MOD_DIR/build/libs/"ntm-testhelper-*.jar 2>/dev/null | head -1 || true)
    if [ -z "$HELPER_JAR" ]; then
        log "✗ testhelper jar not found after build"
        exit 1
    fi
    log "✓ helper jar: $(basename "$HELPER_JAR")"
fi

# ─── 3-5. Stop / deploy / restart (skip if --no-restart) ────────
if [ "$NO_RESTART" = false ]; then
    # 3. Stop server
    log "▶ stop server on :25565"
    powershell.exe -NoProfile -Command "
        \$p = (Get-NetTCPConnection -LocalPort 25565 -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty OwningProcess);
        if (\$p) { Stop-Process -Id \$p -Force; Start-Sleep -Seconds 3; Write-Output \"stopped PID=\$p\" }
        else { Write-Output 'no process on :25565' }
    " || true

    # 4. Deploy jars
    log "▶ deploy jars → $TESTSERVER/mods/"
    # Remove old NTM main jar only (keep testhelper unless --also-helper replaces it)
    for old in "$TESTSERVER/mods/"ntm-*.jar; do
        [[ "$old" == *testhelper* ]] || rm -f "$old"
    done
    cp "$NTM_JAR" "$TESTSERVER/mods/"
    if [ -n "$HELPER_JAR" ]; then
        rm -f "$TESTSERVER/mods/"ntm-testhelper-*.jar
        cp "$HELPER_JAR" "$TESTSERVER/mods/"
    fi
    log "  mods: $(ls -1 "$TESTSERVER/mods/"*.jar 2>/dev/null | xargs -I{} basename {} | tr '\n' ' ')"

    # 5. Start server and wait for Done
    log "▶ start server"
    rm -f "$TESTSERVER/startup.log"
    (cd "$TESTSERVER" && nohup java -Xms1G -Xmx3G -XX:+UseG1GC \
        -jar forge-1.12.2-14.23.5.2860.jar nogui \
        > startup.log 2>&1 &)

    log "  waiting for server ready…"
    SECONDS=0
    until grep -qE "Done \(|FAILED|Caused by|Exception in server tick loop" \
            "$TESTSERVER/startup.log" 2>/dev/null; do
        if [ $SECONDS -gt 120 ]; then
            log "✗ server start timeout (120s)"
            tail -20 "$TESTSERVER/startup.log"
            exit 1
        fi
        sleep 2
    done

    if grep -qE "FAILED|Caused by|Exception in server tick loop" \
            "$TESTSERVER/startup.log"; then
        log "✗ SERVER START FAILED"
        tail -20 "$TESTSERVER/startup.log"
        exit 1
    fi

    DONE_LINE=$(grep -oE "Done \([0-9.,]+s\)" "$TESTSERVER/startup.log" | head -1)
    log "✓ server up ($DONE_LINE)"
fi

# ─── 6. Run scenarios ───────────────────────────────────────────
cd "$SCRIPT_DIR"
TOTAL_FAILED=0
RESULTS=()

for SCENARIO in "${SCENARIOS[@]}"; do
    SCENARIO_FILE="scenarios/${SCENARIO}.js"
    if [ ! -f "$SCENARIO_FILE" ]; then
        log "✗ scenario not found: $SCENARIO_FILE"
        RESULTS+=("MISSING  $SCENARIO")
        TOTAL_FAILED=$((TOTAL_FAILED + 1))
        continue
    fi

    log "▶ scenario: $SCENARIO"
    if node src/scenario-runner.js "$SCENARIO_FILE"; then
        RESULTS+=("PASS     $SCENARIO")
        log "✓ $SCENARIO PASS"
    else
        RESULTS+=("FAIL     $SCENARIO")
        TOTAL_FAILED=$((TOTAL_FAILED + 1))
        log "✗ $SCENARIO FAIL"
    fi
done

# ─── 7. Aggregate report ────────────────────────────────────────
echo ""
log "=== AGGREGATE REPORT ==="
for r in "${RESULTS[@]}"; do
    log "  $r"
done
echo ""

if [ $TOTAL_FAILED -eq 0 ]; then
    log "✓ ALL ${#SCENARIOS[@]} SCENARIO(S) PASS"
    exit 0
else
    log "✗ $TOTAL_FAILED / ${#SCENARIOS[@]} FAILED"
    exit 1
fi
