# start-all.ps1 — Inicia todos os serviços do backend RedGit
#
# Pré-requisitos:
#   - Java 21 e Maven no PATH
#   - Redis rodando: docker compose up -d (na raiz do projeto)
#   - Variável de ambiente JWT_SECRET definida na sessão ou no sistema
#
# Uso: .\start-all.ps1

$BackendDir = $PSScriptRoot

# Encerra processos Java existentes para evitar conflito de porta
Write-Host "Encerrando processos Java existentes..." -ForegroundColor Yellow
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

# Serviços ativos no fluxo principal
$services = @(
    [PSCustomObject]@{ Name = "Cadastro e Login"; Path = "API - Cadastro e Login\API - Cadastro e Login"; Port = 8081 }
    [PSCustomObject]@{ Name = "Ideias Hub";       Path = "API - Ideias Hub\API - Ideias Hub";             Port = 8082 }
    [PSCustomObject]@{ Name = "Profile";          Path = "API - Profile";                                 Port = 8083 }

    # Serviços órfãos — descomentar após decisão em TASK-03:
    # [PSCustomObject]@{ Name = "Reputation"; Path = "API - Reputation"; Port = 8084 }
    # [PSCustomObject]@{ Name = "Trending";   Path = "API - Trending";   Port = 8085 }
)

foreach ($svc in $services) {
    $fullPath = Join-Path $BackendDir $svc.Path
    $name     = $svc.Name
    $port     = $svc.Port

    Write-Host "Iniciando $name na porta $port..." -ForegroundColor Cyan

    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "cd '$fullPath'; `$host.UI.RawUI.WindowTitle = 'RedGit – $name ($port)'; mvn spring-boot:run -q"
    )
}

Write-Host ""
Write-Host "Serviços iniciados:" -ForegroundColor Green
Write-Host "  Cadastro e Login  -> http://localhost:8081"
Write-Host "  Ideias Hub        -> http://localhost:8082"
Write-Host "  Profile           -> http://localhost:8083"
