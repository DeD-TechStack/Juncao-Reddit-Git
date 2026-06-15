# start-all.ps1 - Inicia todos os servicos do backend RedGit
#
# Pre-requisitos:
#   - Java 21 e Maven no PATH
#   - Redis rodando: docker compose up -d (na raiz do projeto)
#   - Variaveis de ambiente definidas na sessao, no sistema, ou em Backend\.env:
#       JWT_SECRET, INTERNAL_SERVICE_SECRET
#
# Uso: .\start-all.ps1

$BackendDir = $PSScriptRoot

# Carrega Backend\.env, se existir, definindo variaveis que ainda nao estejam presentes
# na sessao atual (variaveis ja definidas via $env: continuam tendo prioridade)
$envFile = Join-Path $BackendDir ".env"
if (Test-Path $envFile) {
    foreach ($line in Get-Content $envFile) {
        $trimmed = $line.Trim()
        if ($trimmed -eq "" -or $trimmed.StartsWith("#")) {
            continue
        }
        $parts = $trimmed -split "=", 2
        if ($parts.Count -ne 2) {
            continue
        }
        $name  = $parts[0].Trim()
        $value = $parts[1].Trim().Trim("'", '"')
        if ([string]::IsNullOrEmpty([Environment]::GetEnvironmentVariable($name)) -and -not [string]::IsNullOrEmpty($value)) {
            [Environment]::SetEnvironmentVariable($name, $value)
        }
    }
}

# Valida variaveis de ambiente obrigatorias antes de iniciar qualquer servico
$requiredEnvVars = @("JWT_SECRET", "INTERNAL_SERVICE_SECRET")
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
    [PSCustomObject]@{ Name = "Auth";             Path = "API - Auth";                                    Port = 8081 }
    [PSCustomObject]@{ Name = "Ideias Hub";       Path = "API - Ideias Hub";                              Port = 8082 }
    [PSCustomObject]@{ Name = "Profile";          Path = "API - Profile";                                 Port = 8083 }
    [PSCustomObject]@{ Name = "Reputation";       Path = "API - Reputation";                              Port = 8084 }
    [PSCustomObject]@{ Name = "Trending";         Path = "API - Trending";                                Port = 8085 }
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
Write-Host "  Auth              -> http://localhost:8081"
Write-Host "  Ideias Hub        -> http://localhost:8082"
Write-Host "  Profile           -> http://localhost:8083"
Write-Host "  Reputation        -> http://localhost:8084"
Write-Host "  Trending          -> http://localhost:8085"
