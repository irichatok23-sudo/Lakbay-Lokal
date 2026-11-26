param([string]$File, [string]$Pattern)
$lines = Get-Content $File
for ($i = 0; $i -lt $lines.Length; $i++) {
    if ($lines[$i] -match $Pattern) {
        Write-Output (($i + 1).ToString() + ': ' + $lines[$i])
    }
}
