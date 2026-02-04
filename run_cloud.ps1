# Script to run Core Service with Cloud Profile and .env variables

# 1. Set JAVA_HOME explicitly to fix "JAVA_HOME not found" error
$javaPath = "C:\Program Files\Java\jdk-21"
if (Test-Path $javaPath) {
    $env:JAVA_HOME = $javaPath
    Write-Host "✅ Succeeded setting JAVA_HOME to: $env:JAVA_HOME" -ForegroundColor Green
} else {
    Write-Host "⚠️ Warning: JDK path '$javaPath' not found. Please check your installation." -ForegroundColor Yellow
}

# 2. Load Environment Variables from .env file
if (Test-Path .env) {
    Write-Host "READING .env file..." -ForegroundColor Cyan
    $envParams = Get-Content .env -Encoding UTF8 | Where-Object { $_ -match '=' -and -not ($_ -match '^#') -and -not ([string]::IsNullOrWhiteSpace($_)) }
    foreach ($line in $envParams) {
        $parts = $line.split('=', 2)
        if ($parts.Count -eq 2) {
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()
            # Remove quotes if present
            $value = $value -replace '^"|"$', ''
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
            Write-Host "Loaded Env: $name" -ForegroundColor DarkGray
        }
    }
} else {
    Write-Host "⚠️ .env file not found!" -ForegroundColor Red
}

# 3. Run the application
Write-Host "`n🚀 Starting Core Service (Cloud Profile)..." -ForegroundColor Cyan
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=cloud"
