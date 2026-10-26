#!/usr/bin/env bash
set -euo pipefail

YANDEX_APP="${YANDEX_APP:-/Applications/Yandex.app}"
CACHE_ROOT="${SE_CACHE_PATH:-${XDG_CACHE_HOME:-$HOME/.cache}/selenium}"

if [ ! -f "$YANDEX_APP/Contents/Info.plist" ]; then
  echo "Яндекс Браузер не найден: $YANDEX_APP" >&2
  exit 1
fi

CHROMIUM_VERSION=$(plutil -extract CFBundleChromiumShortVersionString raw -o - "$YANDEX_APP/Contents/Info.plist")
MAJOR="${CHROMIUM_VERSION%%.*}"
echo "Chromium в Яндекс Браузере: $CHROMIUM_VERSION (нужен chromedriver $MAJOR.x)"

case "$(uname -s)-$(uname -m)" in
  Darwin-arm64) PLATFORM="mac-arm64" ;;
  Darwin-x86_64) PLATFORM="mac-x64" ;;
  Linux-x86_64) PLATFORM="linux64" ;;
  Linux-aarch64) PLATFORM="linux-arm64" ;;
  MINGW*|MSYS*|CYGWIN*)
    case "$(uname -m)" in
      x86_64|amd64) PLATFORM="win64" ;;
      *) PLATFORM="win32" ;;
    esac
    ;;
  *) echo "Неизвестная платформа: $(uname -s)-$(uname -m)" >&2; exit 1 ;;
esac

WORK_DIR=$(mktemp -d)
CATALOG="$WORK_DIR/versions.json"
ZIP="$WORK_DIR/chromedriver.zip"
trap 'rm -rf "$WORK_DIR"' EXIT

curl -fsSL --max-time 120 \
  https://googlechromelabs.github.io/chrome-for-testing/known-good-versions-with-downloads.json \
  -o "$CATALOG"

URL=$(python3 - "$CATALOG" "$MAJOR" "$PLATFORM" <<'PY'
import json, sys
catalog, major, platform = sys.argv[1], sys.argv[2], sys.argv[3]
data = json.load(open(catalog))
versions = [v for v in data["versions"] if v["version"].startswith(major + ".")]
for version in reversed(versions):
    for download in version["downloads"].get("chromedriver", []):
        if download["platform"] == platform:
            print(download["url"])
            sys.exit(0)
sys.exit("не найден chromedriver для платформы " + platform)
PY
)

echo "Скачиваем: $URL"
curl -fsSL --max-time 180 "$URL" -o "$ZIP"
unzip -o -q "$ZIP" -d "$WORK_DIR/driver"

DRIVER_NAME=$(find "$WORK_DIR/driver" -type f \( -name chromedriver -o -name chromedriver.exe \) | head -1)
DRIVER_VERSION=$(basename "$(dirname "$(dirname "$URL")")")
TARGET_DIR="$CACHE_ROOT/chromedriver/$PLATFORM/$DRIVER_VERSION"

mkdir -p "$TARGET_DIR"
cp "$DRIVER_NAME" "$TARGET_DIR/chromedriver"
chmod +x "$TARGET_DIR/chromedriver"

echo "Установлено: $TARGET_DIR/chromedriver"
"$TARGET_DIR/chromedriver" --version
