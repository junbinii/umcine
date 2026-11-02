. "$PSScriptRoot\scripts\Environment.ps1"
$env:APP_CORS_ENABLED = 'true'
& "$PSScriptRoot\gradlew.bat" --console=plain test bootJar
exit $LASTEXITCODE
