@echo off

SET BASEDIR=%~dp0
SET EXECUTABLE=%BASEDIR%internal\run.bat

REM setup the JVM
IF "x%JAVA_HOME%" == "x" (
  SET JAVA=java
  ECHO JAVA_HOME is not set. Unexpected results may occur.
  ECHO Set JAVA_HOME to the directory of your local JDK to avoid this message.
) ELSE (
  IF NOT EXIST "%JAVA_HOME%" (
    ECHO JAVA_HOME "%JAVA_HOME%" path doesn't exist
    GOTO Done
  ) ELSE (
    IF NOT EXIST "%JAVA_HOME%\bin\java.exe" (
      ECHO "%JAVA_HOME%\bin\java.exe" does not exist
      GOTO Done
    )
    ECHO Setting JAVA property to "%JAVA_HOME%\bin\java"
    SET JAVA="%JAVA_HOME%\bin\java"
  )
)

IF NOT "x%JAVA_OPTS%" == "x" (
  ECHO JAVA_OPTS: %JAVA_OPTS%
)

REM check Java version >= 17
SET EXPECTED_JAVA_VERSION=17
FOR /f "tokens=3" %%g IN ('JAVA -version 2^>^&1 ^| findstr /i "version"') DO (
  SET JAVA_VERSION=%%g
)
SET JAVA_VERSION=%JAVA_VERSION:"=%
ECHO Java version is %JAVA_VERSION%
FOR /f "delims=. tokens=1" %%v in ("%JAVA_VERSION%") do (
  IF %%v LSS %EXPECTED_JAVA_VERSION% (
    ECHO You mus use at least JDK 17 to start Camunda Platform Run.
    GOTO Done
  )
)

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