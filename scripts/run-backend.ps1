# Loads ../.env into this process's environment (values are never printed), then starts the backend.
# Usage (from anywhere): powershell -File scripts\run-backend.ps1
$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root '.env'
if (-not (Test-Path $envFile)) { Write-Error ".env not found. Copy .env.example to .env and fill it in."; exit 1 }

Get-Content $envFile | ForEach-Object {
  if ($_ -match '^\s*([A-Z][A-Z0-9_]*)=(.*)$') {
    [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
  }
}

Set-Location (Join-Path $root 'backend')
& .\mvnw.cmd -B spring-boot:run
