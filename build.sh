#!/usr/bin/env bash

VERSION="1.7.2-SNAPSHOT"

LTS_BIN="${BASH_SOURCE-$0}"
LTS_BIN="$(dirname "${LTS_BIN}")"
LTS_Bin_Dir="$(cd "${LTS_BIN}"; pwd)"

cd $LTS_Bin_Dir

mvn clean install -U -DskipTests

Dist_Bin_Dir="$LTS_Bin_Dir/dist/lts-$VERSION-bin"
mkdir -p $Dist_Bin_Dir

Dist_Bin_Dir="$(cd "$(dirname "${Dist_Bin_Dir}/.")"; pwd)"

mkdir -p $Dist_Bin_Dir

# 打包
Startup_Dir="$LTS_Bin_Dir/lts-startup/"
cd $Startup_Dir
mvn clean assembly:assembly -DskipTests -Pdefault

cp -rf $Startup_Dir/target/lts-bin/lts/*  $Dist_Bin_Dir

mkdir -p $Dist_Bin_Dir/war/jetty/lib
mvn clean assembly:assembly -DskipTests -Plts-admin
cp -rf $Startup_Dir/target/lts-bin/lts/lib  $Dist_Bin_Dir/war/jetty
cp -rf $LTS_Bin_Dir/lts-admin/target/lts-admin-$VERSION.war $Dist_Bin_Dir/war/lts-admin.war

 cd $LTS_Bin_Dir/dist
 zip -r lts-$VERSION-bin.zip lts-$VERSION-bin/*
 rm -rf lts-$VERSION-bin
