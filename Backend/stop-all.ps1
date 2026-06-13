# stop-all.ps1 — Encerra os serviços do backend RedGit
#
# Estratégia: varre as portas conhecidas via netstat e encerra apenas
# processos Java encontrados nessas portas (não afeta outros programas).
#
# Uso: .\stop-all.ps1

# Portas dos serviços ativos (manter alinhado com start-all.ps1)
$ports = @(8081, 8082, 8083)

# Descomenttar após TASK-03:
# $ports += 8084  # Reputation
# $ports += 9999  # Trending (porta: definir após TASK-01)

$stopped = 0

foreach ($port in $ports) {
    $netstatLines = netstat -ano | Where-Object { $_ -match "TCP\s+\S+:$port\s+\S+\s+LISTENING" }

    foreach ($line in $netstatLines) {
        $parts = $line.Trim() -split '\s+'
        $pid   = $parts[-1]

        if ($pid -match '^\d+$') {
            $proc = Get-Process -Id $pid -ErrorAction SilentlyContinue
            if ($proc -and $proc.ProcessName -eq "java") {
                Write-Host "Encerrando $($proc.ProcessName) na porta $port (PID $pid)..." -ForegroundColor Yellow
                Stop-Process -Id $pid -Force
                $stopped++
            }
        }
    }
}

if ($stopped -eq 0) {
    Write-Host "Nenhum processo Java encontrado nas portas monitoradas." -ForegroundColor Gray
} else {
    Write-Host "$stopped processo(s) encerrado(s)." -ForegroundColor Green
}
