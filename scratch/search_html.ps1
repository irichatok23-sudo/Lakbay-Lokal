param([string]$Pattern)
$lines = Get-Content 'src\main\resources\web\index.html'
for ($i = 0; $i -lt $lines.Length; $i++) {
    if ($lines[$i] -match $Pattern) {
        Write-Output (($i + 1).ToString() + ': ' + $lines[$i])
    }
}
