@echo off

set JVMFLAGS=-Dfile.encoding=UTF-8
set BASE_HOME=%~dp0%..\
md "%BASE_HOME%\logs"

set CLASSPATH=%BASE_HOME%\war\jetty\lib\*;%CLASSPATH%

set LTS_MAIN=com.github.ltsopensource.startup.admin.JettyContainer

echo LTS-Admin started

java -cp "%CLASSPATH%" %JVMFLAGS% %LTS_MAIN% %BASE_HOME% %*

pause>null



