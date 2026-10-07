# Deuxieme passe de generation ChatUniTest (IFT3913 tache 2) : methodes non ciblees par generer.ps1.
# Meme configuration (profil tache2 : 1 test par methode, 2 rondes de reparation).
# Usage (depuis la racine du depot) : powershell -ExecutionPolicy Bypass -File tache2-rapports\generer-suite.ps1
$methodes = @(
    'EndianUtils#readShortLE', 'EndianUtils#readShortBE', 'EndianUtils#readUShortLE',
    'EndianUtils#readUIntLE', 'EndianUtils#readUIntBE', 'EndianUtils#readIntBE',
    'EndianUtils#readIntME', 'EndianUtils#readUE7', 'EndianUtils#getShortLE',
    'EndianUtils#getUShortLE', 'EndianUtils#getUShortBE', 'EndianUtils#getIntLE',
    'EndianUtils#getUIntBE',
    'FilenameUtils#getSanitizedEmbeddedFileName', 'FilenameUtils#getSanitizedEmbeddedFilePath',
    'FilenameUtils#resolveWithin', 'FilenameUtils#calculateExtension'
)
New-Item -ItemType Directory -Force tache2-rapports\logs-suite | Out-Null
foreach ($m in $methodes) {
    $log = "tache2-rapports\logs-suite\" + ($m -replace '#', '_') + ".log"
    Write-Output "$(Get-Date -Format HH:mm:ss) DEBUT $m"
    cmd /c "mvn -B -ntp -pl tika-core -Ptache2 io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:method -DselectMethod=$m -Dcheckstyle.skip -Drat.skip > $log 2>&1"
    Write-Output "$(Get-Date -Format HH:mm:ss) FIN $m (exit $LASTEXITCODE)"
}
Write-Output "TERMINE"
