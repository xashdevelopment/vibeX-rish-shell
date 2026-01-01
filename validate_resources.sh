#!/bin/bash
# Android Resource Validator Script
# Validates all XML resources before committing

set -e

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/tmp/android-sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin

AAPT=/tmp/android-sdk/platform-tools/aapt

echo "=== Android Resource Validation ==="
echo ""

ERRORS=0

# Function to validate XML file
validate_xml() {
    local file=$1
    echo -n "Checking: $file ... "
    
    # Check if file exists and is readable
    if [ ! -f "$file" ]; then
        echo "ERROR: File not found"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
    
    # Check for valid XML syntax using xmllint if available
    if command -v xmllint &> /dev/null; then
        if ! xmllint --noout "$file" 2>/dev/null; then
            echo "ERROR: Invalid XML"
            ERRORS=$((ERRORS + 1))
            return 1
        fi
    fi
    
    # Check for uppercase characters in resource filenames (not in manifest)
    local filename=$(basename "$file")
    local dir=$(dirname "$file")
    
    # Only check for uppercase in resource filenames, not manifest
    if [[ "$filename" =~ [A-Z] ]] && [[ "$dir" == *"res"* ]]; then
        echo "ERROR: Uppercase in resource filename (Android requires lowercase)"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
    
    echo "OK"
    return 0
}

# Find and validate all XML resource files
PROJECT_DIR=${1:-/workspace/vibeX-project}

echo "Project: $PROJECT_DIR"
echo ""
echo "Validating resource files..."
echo ""

cd "$PROJECT_DIR/app/src/main/res"

# Check all drawable XMLs
if [ -d "drawable" ]; then
    echo "--- Drawable Resources ---"
    for f in drawable/*.xml; do
        [ -f "$f" ] && validate_xml "$f"
    done
    echo ""
fi

# Check all layout XMLs
if [ -d "layout" ]; then
    echo "--- Layout Resources ---"
    for f in layout/*.xml; do
        [ -f "$f" ] && validate_xml "$f"
    done
    echo ""
fi

# Check all values XMLs
if [ -d "values" ]; then
    echo "--- Values Resources ---"
    for f in values/*.xml; do
        [ -f "$f" ] && validate_xml "$f"
    done
    echo ""
fi

# Check values subdirectories
for dir in values-*; do
    if [ -d "$dir" ]; then
        echo "--- $(basename $dir) Resources ---"
        for f in $dir/*.xml; do
            [ -f "$f" ] && validate_xml "$f"
        done
        echo ""
    fi
done

# Check XML resources
if [ -d "xml" ]; then
    echo "--- XML Resources ---"
    for f in xml/*.xml; do
        [ -f "$f" ] && validate_xml "$f"
    done
    echo ""
fi

# Check mipmap XMLs (adaptive icons)
if [ -d "mipmap-anydpi-v26" ]; then
    echo "--- Mipmap XMLs (Adaptive Icons) ---"
    for f in mipmap-anydpi-v26/*.xml; do
        [ -f "$f" ] && validate_xml "$f"
    done
    echo ""
fi

# Return to project root for manifest check
cd "$PROJECT_DIR/app/src/main"

# Check AndroidManifest.xml
if [ -f "AndroidManifest.xml" ]; then
    echo "--- Android Manifest ---"
    validate_xml "AndroidManifest.xml"
    echo ""
fi

echo "=== Validation Complete ==="
if [ $ERRORS -eq 0 ]; then
    echo "✓ All resources validated successfully!"
    exit 0
else
    echo "✗ Found $ERRORS error(s)"
    exit 1
fi
