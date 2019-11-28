#!/usr/bin/env bash
VERSION="1.8.0-SNAPSHOT"

if [ -z "$1" ]
  then
    echo -e "please pass docker 'registry location'. Eg:\n'sh build-docker.sh 10.168.1.136:5000'"
    exit 0;
fi
docker build -t seahuang/lts:$VERSION $(dirname "$0")
docker tag seahuang/lts:$VERSION $1/seahuang/lts:$VERSION
docker push $1/seahuang/lts:$VERSION