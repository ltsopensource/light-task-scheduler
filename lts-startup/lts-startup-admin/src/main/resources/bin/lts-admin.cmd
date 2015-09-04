@echo off

set BASE_HOME=%~dp0%..
md "%BASE_HOME%\logs"

set CLASSPATH=%BASE_HOME%\war\lib\*;%CLASSPATH%

set LTS_MAIN=com.lts.startup.JettyContainer

echo LTS-Admin started

java -cp "%CLASSPATH%" %LTS_MAIN% "%BASE_HOME%\war" %*

pause>null

