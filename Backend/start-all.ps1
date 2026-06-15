# start-all.ps1 - Inicia todos os servicos do backend RedGit
#
# Pre-requisitos:
#   - Java 21 e Maven no PATH
#   - Redis rodando: docker compose up -d (na raiz do projeto)
#   - Variaveis de ambiente definidas na sessao ou no sistema:
#       JWT_SECRET, SECURITY_JWT_SECRET_KEY, INTERNAL_SERVICE_SECRET
#
# Uso: .\start-all.ps1

$BackendDir = $PSScriptRoot

# Valida variaveis de ambiente obrigatorias antes de iniciar qualquer servico
$requiredEnvVars = @("JWT_SECRET", "SECURITY_JWT_SECRET_KEY", "INTERNAL_SERVICE_SECRET")
$missingEnvVars = @()

foreach ($varName in $requiredEnvVars) {
    $value = [Environment]::GetEnvironmentVariable($varName)
    if ([string]::IsNullOrEmpty($value)) {
        $missingEnvVars += $varName
    }
}

if ($missingEnvVars.Count -gt 0) {
    Write-Host "Erro: variaveis de ambiente obrigatorias nao definidas:" -ForegroundColor Red
    foreach ($varName in $missingEnvVars) {
        Write-Host "  - $varName" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "Defina cada variavel na sessao atual antes de executar este script. Exemplo:" -ForegroundColor Yellow
    foreach ($varName in $missingEnvVars) {
        Write-Host "  `$env:$varName = 'valor-do-segredo'"
    }
    Write-Host ""
    Write-Host "Nenhum servico foi iniciado." -ForegroundColor Red
    exit 1
}

# Encerra processos Java existentes para evitar conflito de porta
Write-Host "Encerrando processos Java existentes..." -ForegroundColor Yellow
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

# Servicos ativos no fluxo principal
$services = @(
    [PSCustomObject]@{ Name = "Cadastro e Login"; Path = "API - Cadastro e Login\API - Cadastro e Login"; Port = 8081 }
    [PSCustomObject]@{ Name = "Ideias Hub";       Path = "API - Ideias Hub\API - Ideias Hub";             Port = 8082 }
    [PSCustomObject]@{ Name = "Profile";          Path = "API - Profile";                                 Port = 8083 }

    # Servicos orfaos - descomentar apos decisao em TASK-03:
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
        "cd '$fullPath'; `$host.UI.RawUI.WindowTitle = 'RedGit - $name ($port)'; mvn spring-boot:run -q"
    )
}

Write-Host ""
Write-Host "Servicos iniciados:" -ForegroundColor Green
Write-Host "  Cadastro e Login  -> http://localhost:8081"
Write-Host "  Ideias Hub        -> http://localhost:8082"
Write-Host "  Profile           -> http://localhost:8083"
