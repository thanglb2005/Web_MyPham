# Import DB.sql + refund.sql + FlashSales.sql vao SQL Server trong Docker (database WebMyPham).
# Chay tu thu muc goc OneShop:  .\docker\scripts\import-db.ps1
#
# Dung file tam + docker cp (UTF-8) thay vi pipe truc tiep - tranh loi font/chu Viet khi import tu PowerShell.
#
# Truoc khi chay:
#   docker compose stop app
#   docker compose down -v
#   docker compose up -d sqlserver db-init
#   Doi db-init xong, roi chay script nay.
#   docker compose up -d --build app

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Set-Location $Root

$EnvFile = Join-Path $Root ".env"
if (-not (Test-Path $EnvFile)) {
    Write-Host "Khong tim thay .env - dung mat khau mac dinh compose (YourStrong!Passw0rd)."
    $SaPassword = "YourStrong!Passw0rd"
} else {
    $SaPassword = $null
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match '^\s*MSSQL_SA_PASSWORD\s*=\s*(.+)\s*$') {
            $SaPassword = $matches[1].Trim().Trim('"').Trim("'")
        }
    }
    if (-not $SaPassword) { $SaPassword = "YourStrong!Passw0rd" }
}

function Test-Container {
    docker ps --filter "name=oneshop-sqlserver" --format "{{.Names}}" 2>$null | Select-Object -First 1
}

if (-not (Test-Container)) {
    Write-Error "Container oneshop-sqlserver khong chay. Hay: docker compose up -d sqlserver db-init"
}

$DbSql = Join-Path $Root "DB.sql"
$RefundSql = Join-Path $Root "refund.sql"
$FlashSql = Join-Path $Root "FlashSales.sql"

foreach ($f in @($DbSql, $RefundSql, $FlashSql)) {
    if (-not (Test-Path $f)) {
        Write-Error "Thieu file: $f"
    }
}

$Utf8NoBom = New-Object System.Text.UTF8Encoding $false

function Invoke-SqlFileInContainer {
    param(
        [string]$HostPath,
        [string]$RemoteName
    )
    docker cp $HostPath "oneshop-sqlserver:/tmp/$RemoteName"
    if ($LASTEXITCODE -ne 0) { Write-Error "docker cp that bai: $HostPath" }
    docker exec oneshop-sqlserver /opt/mssql-tools18/bin/sqlcmd `
        -S localhost -U sa -P $SaPassword -C -b `
        -i "/tmp/$RemoteName"
    if ($LASTEXITCODE -ne 0) { Write-Error "sqlcmd loi voi file /tmp/$RemoteName" }
}

Write-Host "=== 1/3 DB.sql (bo 4 dong dau, ghi UTF-8 roi copy vao container) ===" -ForegroundColor Cyan
$lines = Get-Content $DbSql -Encoding UTF8
$body = ($lines | Select-Object -Skip 4) -join [Environment]::NewLine
$mergedDb = Join-Path $env:TEMP "oneshop_DB_import.sql"
$batch = "USE WebMyPham;" + [Environment]::NewLine + "GO" + [Environment]::NewLine + $body
[System.IO.File]::WriteAllText($mergedDb, $batch, $Utf8NoBom)
try {
    Invoke-SqlFileInContainer -HostPath $mergedDb -RemoteName "oneshop_DB_import.sql"
} finally {
    Remove-Item -LiteralPath $mergedDb -Force -ErrorAction SilentlyContinue
}

Write-Host "=== 2/3 refund.sql ===" -ForegroundColor Cyan
Invoke-SqlFileInContainer -HostPath $RefundSql -RemoteName "oneshop_refund.sql"

Write-Host "=== 3/3 FlashSales.sql ===" -ForegroundColor Cyan
Invoke-SqlFileInContainer -HostPath $FlashSql -RemoteName "oneshop_flash.sql"

Write-Host "=== Xong. Kiem tra: ===" -ForegroundColor Green
docker exec oneshop-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P $SaPassword -C -d WebMyPham -Q "SELECT TOP 1 category_name FROM categories;"
docker exec oneshop-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P $SaPassword -C -d WebMyPham -Q "SELECT COUNT(*) AS users FROM [user]; SELECT COUNT(*) AS products FROM products;"
Write-Host "Neu category_name hien dung tieng Viet, import OK. Khoi dong app: docker compose up -d --build app" -ForegroundColor Yellow
