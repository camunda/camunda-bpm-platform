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
call "%EXECUTABLE%" start %CMD_LINE_ARGS%
GOTO Done

:StartWithArguments
call "%EXECUTABLE%" start %CMD_LINE_ARGS%

:Done