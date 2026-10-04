# Genere les tests ChatUniTest methode par methode (IFT3913 tache 2).
# Usage (depuis la racine du depot) : powershell -File tache2-rapports\generer.ps1
$methodes = @(
    'EndianUtils#readUShortBE', 'EndianUtils#readIntLE', 'EndianUtils#readLongLE',
    'EndianUtils#readLongBE', 'EndianUtils#getShortBE', 'EndianUtils#getIntBE',
    'EndianUtils#getUIntLE', 'EndianUtils#getLongLE', 'EndianUtils#ubyteToInt',
    'EndianUtils#getUByte',
    'FilenameUtils#calculateExtension', 'FilenameUtils#getName',
    'FilenameUtils#normalize', 'FilenameUtils#getSuffixFromPath'
)
New-Item -ItemType Directory -Force tache2-rapports\logs | Out-Null
foreach ($m in $methodes) {
    $log = "tache2-rapports\logs\" + ($m -replace '#', '_') + ".log"
    Write-Output "$(Get-Date -Format HH:mm:ss) DEBUT $m"
    mvn -B -pl tika-core -Ptache2 io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:method "-DselectMethod=$m" "-Dcheckstyle.skip" "-Drat.skip" *> $log
    Write-Output "$(Get-Date -Format HH:mm:ss) FIN $m (exit $LASTEXITCODE)"
}
Write-Output "TERMINE"
