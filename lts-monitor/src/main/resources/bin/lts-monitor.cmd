@echo off
set /p NODE_NAME="Enter your LtsMonitor config name:"

set JVMFLAGS=-Dfile.encoding=UTF-8
set BASE_HOME=%~dp0%..\
md "%BASE_HOME%\logs"

set CLASSPATH=%BASE_HOME%\lts-monitor\lib\*;%CLASSPATH%

set CONF_HOME=%BASE_HOME%\lts-monitor\conf\%NODE_NAME%

set LTS_MAIN=com.lts.monitor.MonitorAgentStartup

echo LtsMonitor [%NODE_NAME%] started
java -cp "%CLASSPATH%" %JVMFLAGS% %LTS_MAIN% "%CONF_HOME%" %*
