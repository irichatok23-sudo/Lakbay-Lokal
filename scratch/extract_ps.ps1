Add-Type -AssemblyName System.IO.Compression.FileSystem
$docPath = 'C:\Users\ASUS\OneDrive\Documents\ieee-format (1).docx'
$zip = [System.IO.Compression.ZipFile]::OpenRead($docPath)
$entry = $zip.GetEntry('word/document.xml')
$stream = $entry.Open()
$reader = New-Object System.IO.StreamReader($stream)
$xml = $reader.ReadToEnd()
$reader.Close()
$stream.Close()
$zip.Dispose()

$text = $xml -replace '<[^>]+>', ' ' -replace '\s+', ' '
[System.IO.File]::WriteAllText('scratch\manuscript_extracted.txt', $text, [System.Text.Encoding]::UTF8)
Write-Output "Extracted $($text.Length) characters."
