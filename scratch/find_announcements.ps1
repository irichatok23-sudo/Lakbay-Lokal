$lines = Get-Content 'src\main\resources\web\js\app.js'
for ($i = 0; $i -lt $lines.Length; $i++) {
    $l = $lines[$i]
    if ($l -match 'renderAnnouncementsList' -or $l -match 'announcements.cards' -or $l -match 'loadAnnouncements' -or $l -match 'pane.announc') {
        Write-Output (($i + 1).ToString() + ': ' + $l)
    }
}
