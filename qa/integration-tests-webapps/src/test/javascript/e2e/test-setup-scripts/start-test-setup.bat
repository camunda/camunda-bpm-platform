@ECHO OFF

START /min "Start webdriver" webdriver-runner.bat 

START /min "Start camunda BPM Platform" camunda-bpm-runner.bat

START /max "Protractor Runner" protractor-runner.bat

EXIT 0