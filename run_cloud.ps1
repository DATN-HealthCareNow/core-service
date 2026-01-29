# Script to run Core Service with Cloud Profile and .env variables
$envParams = Get-Content .env | Where-Object { $_ -match '=' -and -not ($_ -match '^#') }
foreach ($line in $envParams) {
    $name, $value = $line.split('=', 2)
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), "Process")
    Write-Host "Loaded Env: $name"
}

Write-Host "Starting Core Service (Cloud Profile)..."
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=cloud"
