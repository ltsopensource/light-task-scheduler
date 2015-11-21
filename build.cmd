@echo off

start mvn clean install -DskipTests
echo "LTS: mvn clean install -DskipTests"
echo "LTS: After sub window finished, close it , and press any key to continue" & pause>nul

set VERSION=1.6.3-SNAPSHOT
set BASE_HOME=%~dp0%
set DIST_BIN_DIR=lts-%VERSION%-bin

md "%BASE_HOME%\dist"
md "%BASE_HOME%\dist\%DIST_BIN_DIR%"

set LTS_BIN_DIR=%BASE_HOME%dist\%DIST_BIN_DIR%

set JOB_TRACKER_START_UP_DIR=%BASE_HOME%\lts-startup\lts-startup-jobtracker
cd %JOB_TRACKER_START_UP_DIR%
start mvn assembly:assembly -DskipTests
echo "LTS: mvn assembly:assembly -DskipTests"
echo "LTS: After sub window finished, close it , and press any key to continue" & pause>nul

xcopy /e /y "%JOB_TRACKER_START_UP_DIR%\target\lts-bin\lts" "%LTS_BIN_DIR%"
cd ..\..\

set LTS_ADMIN_START_UP_DIR=%BASE_HOME%/lts-startup/lts-startup-admin
cd %LTS_ADMIN_START_UP_DIR%
start mvn assembly:assembly -DskipTests
echo "LTS: mvn assembly:assembly -DskipTests"
echo "LTS: After sub window finished, close it , and press any key to continue" & pause>nul

xcopy /e /y "%LTS_ADMIN_START_UP_DIR%\target\lts-bin\lts" "%LTS_BIN_DIR%"
xcopy /e /y "%BASE_HOME%\lts-admin\target\lts-admin-%VERSION%.war" "%LTS_BIN_DIR%\lts-admin\lts-admin.war"
cd ..\..\
