$lines = Get-Content 'src\main\resources\web\js\app.js'
for ($i = 0; $i -lt $lines.Length; $i++) {
    $l = $lines[$i]
    if ($l -match 'loadVerification' -or $l -match 'renderVerification' -or $l -match 'openVerification' -or $l -match 'handleVerification' -or $l -match 'verificationStatus' -or $l -match 'pane.verif') {
        Write-Output (($i + 1).ToString() + ': ' + $l)
    }
}
