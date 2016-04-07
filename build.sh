#!/usr/bin/env bash

VERSION="1.6.6"

LTS_BIN="${BASH_SOURCE-$0}"
LTS_BIN="$(dirname "${LTS_BIN}")"
LTS_Bin_Dir="$(cd "${LTS_BIN}"; pwd)"

cd $LTS_Bin_Dir

mvn clean install -U -DskipTests

Dist_Bin_Dir="lts-$VERSION-bin"
mkdir -p $LTS_Bin_Dir/dist/$Dist_Bin_Dir

# JobTracker 的打包
JobTracker_Startup_Dir="$LTS_Bin_Dir/lts-startup/lts-startup-jobtracker"
cd $JobTracker_Startup_Dir
mvn assembly:assembly -DskipTests

# LTS-Admin 打包
LTS_Admin_Startup_Dir="$LTS_Bin_Dir/lts-startup/lts-startup-admin"
cd $LTS_Admin_Startup_Dir
mvn assembly:assembly -DskipTests

# TaskTracker 打包
TaskTracker_Startup_Dir="$LTS_Bin_Dir/lts-startup/lts-startup-tasktracker"
cd $TaskTracker_Startup_Dir
mvn assembly:assembly -DskipTests

# LTS-Monitor 打包
LTS_Monitor_Startup_Dir="$LTS_Bin_Dir/lts-monitor"
cd $LTS_Monitor_Startup_Dir
mvn assembly:assembly -DskipTests


cp -rf $JobTracker_Startup_Dir/target/lts-bin/lts/*  $LTS_Bin_Dir/dist/$Dist_Bin_Dir
cp -rf $LTS_Admin_Startup_Dir/target/lts-bin/lts/*  $LTS_Bin_Dir/dist/$Dist_Bin_Dir
cp -rf $TaskTracker_Startup_Dir/target/lts-bin/lts/*  $LTS_Bin_Dir/dist/$Dist_Bin_Dir
cp -rf $LTS_Monitor_Startup_Dir/target/lts-bin/lts/*  $LTS_Bin_Dir/dist/$Dist_Bin_Dir
cp -rf $LTS_Bin_Dir/lts-admin/target/lts-admin-$VERSION.war $LTS_Bin_Dir/dist/$Dist_Bin_Dir/lts-admin/lts-admin.war

# cd $LTS_Bin_Dir/dist
# zip -r $Dist_Bin_Dir.zip $Dist_Bin_Dir/*
# rm -rf $Dist_Bin_Dir

