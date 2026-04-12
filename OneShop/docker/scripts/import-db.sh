#!/usr/bin/env bash
# Import DB.sql + refund.sql + FlashSales.sql (chay tu thu muc goc OneShop).
# Yeu cau: docker compose up -d sqlserver db-init  (KHONG chay app truoc khi import)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

if [[ -f .env ]]; then
  line="$(grep -E '^MSSQL_SA_PASSWORD=' .env | tail -1)"
  if [[ -n "$line" ]]; then
    MSSQL_SA_PASSWORD="${line#MSSQL_SA_PASSWORD=}"
    MSSQL_SA_PASSWORD="${MSSQL_SA_PASSWORD%$'\r'}"
    MSSQL_SA_PASSWORD="${MSSQL_SA_PASSWORD#\"}"
    MSSQL_SA_PASSWORD="${MSSQL_SA_PASSWORD%\"}"
    export MSSQL_SA_PASSWORD
  fi
fi
: "${MSSQL_SA_PASSWORD:=YourStrong!Passw0rd}"

if ! docker ps --format '{{.Names}}' | grep -q '^oneshop-sqlserver$'; then
  echo "Loi: oneshop-sqlserver khong chay. Chay: docker compose up -d sqlserver db-init" >&2
  exit 1
fi

echo "=== 1/3 DB.sql (bo 4 dong dau: CREATE DATABASE + USE) ==="
{
  printf '%s\r\n' "USE WebMyPham;" "GO"
  tail -n +5 "$ROOT/DB.sql"
} | docker exec -i oneshop-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -b

echo "=== 2/3 refund.sql ==="
docker cp "$ROOT/refund.sql" oneshop-sqlserver:/tmp/refund.sql
docker exec oneshop-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -b -i /tmp/refund.sql

echo "=== 3/3 FlashSales.sql ==="
docker cp "$ROOT/FlashSales.sql" oneshop-sqlserver:/tmp/FlashSales.sql
docker exec oneshop-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -b -i /tmp/FlashSales.sql

echo "=== Xong ==="
docker exec oneshop-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -d WebMyPham -Q "SELECT COUNT(*) AS users FROM [user]; SELECT COUNT(*) AS products FROM products;"
echo "Khoi dong app: docker compose up -d app"
