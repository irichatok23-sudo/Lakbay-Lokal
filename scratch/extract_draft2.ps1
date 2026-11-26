$docPath = 'C:\Users\ASUS\OneDrive\Documents\lakbay lokaLLLL34.docx'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($docPath)
$entry = $zip.GetEntry('word/document.xml')
$stream = $entry.Open()
$reader = New-Object System.IO.StreamReader($stream)
$xml = $reader.ReadToEnd()
$reader.Close()
$stream.Close()
$zip.Dispose()

$text = $xml -replace '<[^>]+>', ' ' -replace '\s+', ' '
[System.IO.File]::WriteAllText('scratch\manuscript_draft2.txt', $text, [System.Text.Encoding]::UTF8)
Write-Output "Extracted $($text.Length) characters from lakbay lokaLLLL34.docx."
