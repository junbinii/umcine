param([switch]$DisableCors)
. "$PSScriptRoot\scripts\Environment.ps1"
$env:APP_CORS_ENABLED = if ($DisableCors) { 'false' } else { 'true' }
Write-Host "Java 21 / Spring Boot / http://localhost:8080 / CORS=$env:APP_CORS_ENABLED"
& "$PSScriptRoot\gradlew.bat" --console=plain bootRun
exit $LASTEXITCODE
