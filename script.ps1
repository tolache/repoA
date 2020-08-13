param ([string] $param1 = "")
#Write-Output "param1: $param1"
$x = $param1.Replace("``n", "`n")
Write-Output "param1:$x"
