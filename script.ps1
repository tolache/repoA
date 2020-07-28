param ([string] $MyParam = "")
$x = $MyParam.Replace("``n", "`n")
Write-Output "You've specified:`n===START===`n$x`n====END===="
