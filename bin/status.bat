:: Batch file that allows easy execution of a tool
:: without the need to set the CLASSAPTH, placing JAR in extension directory
:: or having to type in that long java command (java spase.tool...)
::
:: Version: $Id: status.bat 1 2010-04-30 17:20:44Z todd-king $

@echo off

set THIS_DIR=%~dp0

:: Executes tool
:: The special variable '%*' allows the arguments
:: to be passed into the executable.

echo %THIS_DIR%
java -Djava.ext.dirs=%THIS_DIR%/jar org.spase.registry.server.Status %*
:END
