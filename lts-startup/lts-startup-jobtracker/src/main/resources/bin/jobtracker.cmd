@echo off
set /p NODE_NAME="Enter your jobtracker config name:"

set JVMFLAGS=-Dfile.encoding=UTF-8
set BASE_HOME=%~dp0%..\
md "%BASE_HOME%\logs"

set CLASSPATH=%BASE_HOME%\jobtracker\lib\*;%CLASSPATH%

set CONF_HOME=%BASE_HOME%\jobtracker\conf\%NODE_NAME%

set LTS_MAIN=com.lts.startup.JobTrackerStartup

echo JobTracker [%NODE_NAME%] started
java -cp "%CLASSPATH%" %JVMFLAGS% %LTS_MAIN% "%CONF_HOME%" %*
