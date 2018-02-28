@echo off

start mvn clean install -DskipTests
echo "LTS: mvn clean install -DskipTests"
echo "LTS: After sub window finished, close it , and press any key to continue" & pause>nul

set VERSION=1.7.2-SNAPSHOT
set BASE_HOME=%~dp0%
set DIST_BIN_DIR=lts-%VERSION%-bin

md "%BASE_HOME%\dist"
md "%BASE_HOME%\dist\%DIST_BIN_DIR%"

set LTS_Bin_Dir=%BASE_HOME%dist\%DIST_BIN_DIR%

set Startup_Dir=%BASE_HOME%\lts-startup
cd %Startup_Dir%
start mvn clean assembly:assembly -DskipTests -Pdefault
echo "LTS: mvn clean assembly:assembly -DskipTests -Pdefault"
echo "LTS: After sub window finished, close it , and press any key to continue" & pause>nul

xcopy /e /y "%Startup_Dir%\target\lts-bin\lts" "%LTS_Bin_Dir%"
cd ..\..\

cd %Startup_Dir%
start mvn clean assembly:assembly -DskipTests -Plts-admin
echo "LTS: mvn clean assembly:assembly -DskipTests -Plts-admin"
echo "LTS: After sub window finished, close it , and press any key to continue" & pause>nul

xcopy /e /y "%Startup_Dir%\target\lts-bin\lts\lib" "%LTS_Bin_Dir%\war\jetty\lib"
cd ..\..\

xcopy /e /y "%BASE_HOME%\lts-admin\target\lts-admin-%VERSION%.war" "%LTS_Bin_Dir%\war\lts-admin.war"
cd ..\..\
