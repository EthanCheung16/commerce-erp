param([int]$Port = 8080)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$jdkPath = 'D:\DevelopProgramFiles\jdk-21'
$javaExe = Join-Path $jdkPath 'bin\java.exe'
$jarPath = Join-Path $projectRoot 'target\commerce-erp-1.0.0.jar'
if (-not (Test-Path -LiteralPath $jarPath)) {
    throw '请先在项目目录执行 mvn clean package。'
}
& $javaExe -jar $jarPath "--server.port=$Port"
exit $LASTEXITCODE
