#!/bin/bash

# ==========================================
# CONFIGURATION
# ==========================================
# Replace this with your actual Frontend GitHub URL
REPO_URL="https://github.com/Majkle/NNPRO-REMAX-FE.git"

# The directory relative to this script
# Going up two levels: deployment -> backend -> root
TARGET_DIR="../../NNPRO-REMAX-FE"

# ==========================================
# SCRIPT LOGIC
# ==========================================

# Ensure we are running from the directory where the script is located
cd "$(dirname "$0")"

echo "Checking for frontend project at: $(realpath -m $TARGET_DIR)"

if [ -d "$TARGET_DIR" ]; then
    echo "Frontend directory found. Skipping clone."
else
    echo "Frontend directory not found."
    echo "Cloning from $REPO_URL..."

    git clone "$REPO_URL" "$TARGET_DIR"

    if [ $? -eq 0 ]; then
        echo "Clone successful."
    else
        echo "Clone failed. Please check your URL and internet connection."
        exit 1
    fi
fi