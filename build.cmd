@echo off

start mvn clean install -DskipTests
echo "LTS: mvn clean install -DskipTests"
echo "LTS: After sub window finished, close it , and press any key to continue" & pause>nul

set VERSION=1.6.5-SNAPSHOT
set BASE_HOME=%~dp0%
set DIST_BIN_DIR=lts-%VERSION%-bin

md "%BASE_HOME%\dist"
md "%BASE_HOME%\dist\%DIST_BIN_DIR%"

set LTS_Bin_Dir=%BASE_HOME%dist\%DIST_BIN_DIR%

set JobTracker_Startup_Dir=%BASE_HOME%\lts-startup\lts-startup-jobtracker
cd %JobTracker_Startup_Dir%
start mvn assembly:assembly -DskipTests
echo "LTS: mvn assembly:assembly -DskipTests"
echo "LTS: After sub window finished, close it , and press any key to continue" & pause>nul

xcopy /e /y "%JobTracker_Startup_Dir%\target\lts-bin\lts" "%LTS_Bin_Dir%"
cd ..\..\

set LTS_Admin_Startup_Dir=%BASE_HOME%/lts-startup/lts-startup-admin
cd %LTS_Admin_Startup_Dir%
start mvn assembly:assembly -DskipTests
echo "LTS: mvn assembly:assembly -DskipTests"
echo "LTS: After sub window finished, close it , and press any key to continue" & pause>nul

xcopy /e /y "%LTS_Admin_Startup_Dir%\target\lts-bin\lts" "%LTS_Bin_Dir%"
xcopy /e /y "%BASE_HOME%\lts-admin\target\lts-admin-%VERSION%.war" "%LTS_Bin_Dir%\lts-admin\lts-admin.war"
cd ..\..\

set TaskTracker_Startup_Dir=%BASE_HOME%\lts-startup\lts-startup-tasktracker
cd %TaskTracker_Startup_Dir%
start mvn assembly:assembly -DskipTests
echo "LTS: mvn assembly:assembly -DskipTests"
echo "LTS: After sub window finished, close it , and press any key to continue" & pause>nul

xcopy /e /y "%TaskTracker_Startup_Dir%\target\lts-bin\lts" "%LTS_Bin_Dir%"
cd ..\..\

set LTS_Monitor_Startup_Dir=%BASE_HOME%\lts-monitor
cd %LTS_Monitor_Startup_Dir%
start mvn assembly:assembly -DskipTests
echo "LTS: mvn assembly:assembly -DskipTests"
echo "LTS: After sub window finished, close it , and press any key to continue" & pause>nul

xcopy /e /y "%LTS_Monitor_Startup_Dir%\target\lts-bin\lts" "%LTS_Bin_Dir%"
cd ..\..\
