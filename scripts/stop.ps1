$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$jarPath = Join-Path $projectRoot 'target\commerce-erp-1.0.0.jar'
$jarPattern = '(?i)(?:\s|"|^)' + [regex]::Escape($jarPath) + '(?:\s|"|$)'
$processes = @(Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object { $_.CommandLine -match $jarPattern })
foreach ($process in $processes) {
    Stop-Process -Id $process.ProcessId
    Write-Output "已停止 Commerce ERP，进程 $($process.ProcessId)。"
}
if ($processes.Count -eq 0) { Write-Output '没有找到从本项目 JAR 启动的进程。' }
