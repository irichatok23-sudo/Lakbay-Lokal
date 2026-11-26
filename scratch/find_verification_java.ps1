$lines = Get-Content 'src\main\java\com\lakbaylokal\controller\ApiController.java'
for ($i = 0; $i -lt $lines.Length; $i++) {
    $l = $lines[$i]
    if ($l -match 'verif' -or $l -match 'Verif' -or $l -match 'stakeholder' -or $l -match 'Stakeholder') {
        Write-Output (($i + 1).ToString() + ': ' + $l)
    }
}
