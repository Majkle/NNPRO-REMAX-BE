# ==========================================
# CONFIGURATION
# ==========================================
# Replace this with your actual Frontend GitHub URL
$RepoUrl = "https://github.com/Majkle/NNPRO-REMAX-FE.git"

# The directory relative to this script location
# $PSScriptRoot ensures we start from the folder containing this .ps1 file
$TargetDir = Join-Path -Path $PSScriptRoot -ChildPath "../../NNPRO-REMAX-FE"
$ResolvedPath = $TargetDir # Used for display purposes

# ==========================================
# SCRIPT LOGIC
# ==========================================

try {
    $ResolvedPath = [System.IO.Path]::GetFullPath($TargetDir)
} catch {
    # Path doesn't exist yet, just keep the relative path string
}

Write-Host "Checking for frontend project at: $ResolvedPath" -ForegroundColor Cyan

if (Test-Path -Path $TargetDir) {
    Write-Host "Frontend directory found. Skipping clone." -ForegroundColor Green
} else {
    Write-Host "Frontend directory not found." -ForegroundColor Yellow
    Write-Host "Cloning from $RepoUrl..." -ForegroundColor Cyan

    try {
        git clone $RepoUrl $TargetDir
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Clone successful." -ForegroundColor Green
        } else {
            Write-Host "Clone failed. Git exited with code $LASTEXITCODE" -ForegroundColor Red
        }
    } catch {
        Write-Host "Error executing git command." -ForegroundColor Red
    }
}