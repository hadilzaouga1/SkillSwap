# Lance les 4 microservices Node (session, trust, notification, ai) dans 4 fenêtres PowerShell.
# Usage : depuis la racine du projet ->  .\scripts\demarrer-services-node.ps1
$racine = Split-Path -Parent $PSScriptRoot
foreach ($s in @(@('session-service',8087), @('trust-service',8088), @('notification-service',8089), @('ai-service',8090))) {
  $dossier = Join-Path $racine "backend\$($s[0])"
  if (-not (Test-Path (Join-Path $dossier 'node_modules'))) { Push-Location $dossier; npm install; Pop-Location }
  Start-Process powershell -ArgumentList '-NoExit','-Command',"cd '$dossier'; `$env:PORT=$($s[1]); npm start"
}
Write-Host "4 services lancés (8087 session, 8088 trust, 8089 notification, 8090 ai)."
