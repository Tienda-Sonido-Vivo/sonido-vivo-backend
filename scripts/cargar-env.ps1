$archivoEnv = Join-Path $PSScriptRoot "..\.env"

if (-not (Test-Path $archivoEnv)) {
    throw "No se encontró el archivo .env. Copia primero .env.example a .env."
}

Get-Content $archivoEnv | ForEach-Object {
    $linea = $_.Trim()

    if ($linea -and -not $linea.StartsWith("#") -and $linea.Contains("=")) {
        $partes = $linea -split "=", 2
        $nombre = $partes[0].Trim()
        $valor = $partes[1].Trim()

        [Environment]::SetEnvironmentVariable($nombre, $valor, "Process")
    }
}

Write-Host "Variables de entorno cargadas en esta terminal."
