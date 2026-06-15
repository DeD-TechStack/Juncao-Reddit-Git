# stop-all.ps1 - Encerra os servicos do backend RedGit
#
# Estrategia: varre as portas conhecidas via netstat e encerra apenas
# processos Java encontrados nessas portas (nao afeta outros programas).
#
# Uso: .\stop-all.ps1

# Portas dos servicos ativos (manter alinhado com start-all.ps1)
$ports = @(8081, 8082, 8083, 8084, 8085)

$stopped = 0
$stoppedPids = @{}

foreach ($port in $ports) {
    $netstatLines = netstat -ano | Where-Object { $_ -match "TCP\s+\S+:$port\s+\S+\s+LISTENING" }

    foreach ($line in $netstatLines) {
        $parts = $line.Trim() -split '\s+'
        $procId = $parts[-1]

        if ($procId -match '^\d+$' -and -not $stoppedPids.ContainsKey($procId)) {
            $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
            if ($proc -and $proc.ProcessName -eq "java") {
                Write-Host "Encerrando $($proc.ProcessName) na porta $port (PID $procId)..." -ForegroundColor Yellow
                Stop-Process -Id $procId -Force
                $stoppedPids[$procId] = $true
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
