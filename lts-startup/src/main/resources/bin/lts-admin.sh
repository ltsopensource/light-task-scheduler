#!/usr/bin/env bash

# JVMFLAGS JVM参数可以在这里设置
JVMFLAGS=-Dfile.encoding=UTF-8

LTS_ADMIN_HOME="${BASH_SOURCE-$0}"
LTS_ADMIN_HOME="$(dirname "${LTS_ADMIN_HOME}")"
LTS_ADMIN_HOME="$(cd "${LTS_ADMIN_HOME}"; pwd)"

if [ "$JAVA_HOME" != "" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi

#把lib下的所有jar都加入到classpath中
for i in "$LTS_ADMIN_HOME"/../war/jetty/lib/*.jar
do
	CLASSPATH="$i:$CLASSPATH"
done

# echo $CLASSPATH

mkdir -p $LTS_ADMIN_HOME/../logs
mkdir -p $LTS_ADMIN_HOME/../pid

# 转化为绝对路径
CONF_HOME="$LTS_ADMIN_HOME/../."
CONF_HOME=$(cd "$(dirname "$CONF_HOME")"; pwd)
# echo $CONF_HOME

_LTS_DAEMON_OUT="$LTS_ADMIN_HOME/../logs/lts-admin.out"
LTS_MAIN="com.github.ltsopensource.startup.admin.JettyContainer"

LTS_PID_FILE="$LTS_ADMIN_HOME/../pid/lts-admin.pid"

case $1 in
start)
    echo "Starting LTS LTS-Admin ... "
    if [ -f "$LTS_PID_FILE" ]; then
      if kill -0 `cat "$LTS_PID_FILE"` > /dev/null 2>&1; then
         echo $command already running as process `cat "$LTS_PID_FILE"`.
         exit 0
      fi
    fi
    nohup "$JAVA" -cp "$CLASSPATH" $JVMFLAGS $LTS_MAIN "$CONF_HOME" > "$_LTS_DAEMON_OUT" 2>&1 < /dev/null &

	if [ $? -eq 0 ]
    then
      if /bin/echo -n $! > "$LTS_PID_FILE"
      then
        sleep 1
        echo "STARTED"
      else
        echo "FAILED TO WRITE PID"
        exit 1
      fi
    else
      echo "LTS_ADMIN DID NOT START"
      exit 1
    fi
;;
restart)
    sh $0 stop
    sleep 3
    sh $0 start
;;
stop)
    echo "Stopping LTS LTS-Admin  ... "
    if [ ! -f "$LTS_PID_FILE" ]
    then
      echo "no LTS-Admin to started (could not find file $LTS_PID_FILE)"
    else
      kill -9 $(cat "$LTS_PID_FILE")
      rm "$LTS_PID_FILE"
      echo "STOPPED"
    fi
    exit 0
;;
*)
    echo "Usage: $0 {start|stop|restart}" >&2
esac