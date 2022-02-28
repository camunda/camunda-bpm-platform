@echo off

SET BASEDIR=%~dp0
SET EXECUTABLE=%BASEDIR%internal\run.bat

REM Start detached if no arguments
IF [%~1]==[] GOTO StartDetached

REM Collect arguments to pass to the executable
SET CMD_LINE_ARGS=
:setArgs
IF [%~1]==[] GOTO StartWithArguments
SET CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
SHIFT
GOTO setArgs

:StartDetached
call "%EXECUTABLE%" start --detached
REM open a browser
timeout /t 10 /nobreak > NUL
start http://localhost:8080/camunda-welcome/index.html
GOTO Done

:StartWithArguments
call "%EXECUTABLE%" start %CMD_LINE_ARGS%

:Done