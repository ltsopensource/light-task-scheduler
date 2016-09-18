#!/usr/bin/env bash

# JVMFLAGS JVM参数可以在这里设置
JVMFLAGS=-Dfile.encoding=UTF-8

LTS_MONITOR_HOME="${BASH_SOURCE-$0}"
LTS_MONITOR_HOME="$(dirname "${LTS_MONITOR_HOME}")"
LTS_MONITOR_HOME="$(cd "${LTS_MONITOR_HOME}"; pwd)"

if [ "$JAVA_HOME" != "" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi

mkdir -p $LTS_MONITOR_HOME/../logs
mkdir -p $LTS_MONITOR_HOME/../pid

#把lib下的所有jar都加入到classpath中
for i in "$LTS_MONITOR_HOME"/../lib/*.jar
do
	CLASSPATH="$i:$CLASSPATH"
done

# echo $CLASSPATH

NODE_NAME="$1"  # zoo

# 转化为绝对路径
CONF_HOME="$LTS_MONITOR_HOME/../."
CONF_HOME=$(cd "$(dirname "$CONF_HOME")"; pwd)
CONF_HOME="$CONF_HOME/conf/$NODE_NAME"
# echo $CONF_HOME

_LTS_DAEMON_OUT="$LTS_MONITOR_HOME/../logs/lts-monitor-$NODE_NAME.out"
LTS_MAIN="com.github.ltsopensource.monitor.MonitorAgentStartup"

LTS_PID_FILE="$LTS_MONITOR_HOME/../pid/lts-monitor-$NODE_NAME.pid"

case $2 in
start)
    echo "Starting LTS LTS_MONITOR [$NODE_NAME] ... "
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
      echo "LTS_MONITOR DID NOT START"
      exit 1
    fi
;;
restart)
    sh $0 $1 stop
    sleep 3
    sh $0 $1 start
;;
stop)
    echo "Stopping LTS LTS_MONITOR [$NODE_NAME] ... "
    if [ ! -f "$LTS_PID_FILE" ]
    then
      echo "no lts-monitor to started (could not find file $LTS_PID_FILE)"
    else
      kill -9 $(cat "$LTS_PID_FILE")
      rm "$LTS_PID_FILE"
      echo "STOPPED"
    fi
    exit 0
;;
*)
    echo "Usage: $0 {nodeName} {start|stop|restart}" >&2
esac





