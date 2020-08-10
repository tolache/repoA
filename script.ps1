param ([string] $param1 = "")
$x = $param1.Replace("``n", "`n")
Write-Output "param1:$x"
