$lines = Get-Content 'src\main\resources\web\index.html'
for ($i = 0; $i -lt $lines.Length; $i++) {
    $l = $lines[$i]
    if ($l -match 'pane.verif' -or $l -match 'verification' -or $l -match 'Approve' -or $l -match 'Reject' -or $l -match 'approve' -or $l -match 'reject') {
        Write-Output (($i + 1).ToString() + ': ' + $l)
    }
}
