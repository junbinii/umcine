$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
# Only this process uses Java 21; the system-wide Java setting stays intact.
$javaCandidates = @($env:JAVA_HOME) + @(
    Get-ChildItem 'C:\Program Files\Eclipse Adoptium\jdk-21*', 'C:\Program Files\Java\jdk-21*', "$env:USERPROFILE\.jdks\*21*" -Directory -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty FullName
)
$java21 = $javaCandidates | Where-Object {
    $_ -and (Test-Path (Join-Path $_ 'release')) -and
    ((Get-Content (Join-Path $_ 'release') -Raw) -match 'JAVA_VERSION="21[.\"]')
} | Select-Object -First 1
if (-not $java21) { throw 'Java 21 JDK is required. Set JAVA_HOME to your JDK 21 directory.' }
$env:JAVA_HOME = $java21
$env:PATH = "$java21\bin;$env:PATH"
$envFile = Join-Path $projectRoot '.env'
if (Test-Path $envFile) {
    foreach ($line in Get-Content -LiteralPath $envFile -Encoding UTF8) {
        if ($line -match '^\s*(DB_[A-Z_]+)=(.*)$') {
            $key = $Matches[1]
            $value = $Matches[2].Trim()
            if ($value.StartsWith('"') -and $value.EndsWith('"')) { $value = ConvertFrom-Json $value }
            [Environment]::SetEnvironmentVariable($key, $value, 'Process')
        }
    }
}
if (-not (Test-Path $envFile) -and -not (Test-Path Env:DB_PASSWORD)) {
    throw 'Copy .env.example to .env and enter your MySQL connection settings.'
}
Set-Location -LiteralPath $projectRoot
