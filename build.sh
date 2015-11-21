#!/usr/bin/env bash

VERSION="1.6.3-SNAPSHOT"

LTS_BIN="${BASH_SOURCE-$0}"
LTS_BIN="$(dirname "${LTS_BIN}")"
LTS_BIN_DIR="$(cd "${LTS_BIN}"; pwd)"

cd $LTS_BIN_DIR

mvn clean install -U -DskipTests

DIST_BIN_DIR="lts-$VERSION-bin"
mkdir -p $LTS_BIN_DIR/dist/$DIST_BIN_DIR

# JOB_TRACKER 的打包
JOB_TRACKER_START_UP_DIR="$LTS_BIN_DIR/lts-startup/lts-startup-jobtracker"
cd $JOB_TRACKER_START_UP_DIR
mvn assembly:assembly -DskipTests

# LTS-Admin 打包
LTS_ADMIN_START_UP_DIR="$LTS_BIN_DIR/lts-startup/lts-startup-admin"
cd $LTS_ADMIN_START_UP_DIR
mvn assembly:assembly -DskipTests

# LTS-Admin 打包
LTS_TASK_TRACKER_START_UP_DIR="$LTS_BIN_DIR/lts-startup/lts-startup-tasktracker"
cd $LTS_TASK_TRACKER_START_UP_DIR
mvn assembly:assembly -DskipTests

cp -rf $JOB_TRACKER_START_UP_DIR/target/lts-bin/lts/*  $LTS_BIN_DIR/dist/$DIST_BIN_DIR
cp -rf $LTS_ADMIN_START_UP_DIR/target/lts-bin/lts/*  $LTS_BIN_DIR/dist/$DIST_BIN_DIR
cp -rf $LTS_TASK_TRACKER_START_UP_DIR/target/lts-bin/lts/*  $LTS_BIN_DIR/dist/$DIST_BIN_DIR
cp -rf $LTS_BIN_DIR/lts-admin/target/lts-admin-$VERSION.war $LTS_BIN_DIR/dist/$DIST_BIN_DIR/lts-admin/lts-admin.war

# cd $LTS_BIN_DIR/dist
# zip -r $DIST_BIN_DIR.zip $DIST_BIN_DIR/*
# rm -rf $DIST_BIN_DIR

