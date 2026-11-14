param(
    [string]$Mysql = 'D:\DevelopProgramFiles\mysql-8.4\bin\mysql.exe',
    [string]$DbHost = '127.0.0.1',
    [int]$DbPort = 3306,
    [string]$DbUser = 'root',
    [string]$DbPassword = 'root'
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$previousPassword = $env:MYSQL_PWD
try {
    $env:MYSQL_PWD = $DbPassword
    $mysqlArguments = @('--host', $DbHost, '--port', "$DbPort", '--user', $DbUser, '--default-character-set=utf8mb4', '--batch')
    $exists = & $Mysql @mysqlArguments --skip-column-names --execute="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='commerce_erp'"
    if ($LASTEXITCODE -ne 0) { throw '无法连接 MySQL。' }
    if ([int]$exists -gt 0) {
        throw 'commerce_erp 已存在业务表。为避免重复导入，初始化已停止；已有数据库可直接启动应用。'
    }
    foreach ($name in @('schema.sql', 'initial-data.sql')) {
        $sqlFile = (Join-Path $projectRoot "database\$name").Replace('\', '/')
        & $Mysql @mysqlArguments --execute="source $sqlFile"
        if ($LASTEXITCODE -ne 0) { throw "执行 $name 失败，请检查错误；不要重复向非空表导入。" }
    }
    Write-Output 'Commerce ERP 数据库初始化完成。'
} finally {
    $env:MYSQL_PWD = $previousPassword
}
