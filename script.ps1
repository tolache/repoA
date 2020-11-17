function Send-Keys
{
	notepad
	$wshell = New-Object -ComObject wscript.shell;
	$wshell.AppActivate('Notepad')
	Sleep 1
	$wshell.SendKeys('+2')
	Sleep 1
	$wshell.SendKeys('~')
}
Send-Keys
