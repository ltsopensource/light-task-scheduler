#!/usr/bin/env bash

# JVMFLAGS JVM参数可以在这里设置
JVMFLAGS=-Dfile.encoding=UTF-8

TASK_TRACKER_HOME="${BASH_SOURCE-$0}"
TASK_TRACKER_HOME="$(dirname "${TASK_TRACKER_HOME}")"
TASK_TRACKER_HOME="$(cd "${TASK_TRACKER_HOME}/../"; pwd)"

if [ "$JAVA_HOME" != "" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi

mkdir -p $TASK_TRACKER_HOME/logs
mkdir -p $TASK_TRACKER_HOME/pid

#把lib下的所有jar都加入到classpath中
for i in "$TASK_TRACKER_HOME"/lib/*.jar
do
	CLASSPATH="$i:$CLASSPATH"
done

# echo $CLASSPATH

# 转化为绝对路径
CONF_HOME="$TASK_TRACKER_HOME/conf/"
# echo $CONF_HOME

_LTS_DAEMON_OUT="$TASK_TRACKER_HOME/logs/tasktracker.out"
LTS_MAIN="com.github.ltsopensource.startup.tasktracker.TaskTrackerStartup"

LTS_PID_FILE="$TASK_TRACKER_HOME/pid/tasktracker.pid"

CLASSPATH="$TASK_TRACKER_HOME/conf/:$CLASSPATH"
case $1 in
start)
    echo "Starting LTS TASK_TRACKER ... "
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
      echo "TASK_TRACKER DID NOT START"
      exit 1
    fi
;;
restart)
    sh $0 $1 stop
    sleep 3
    sh $0 $1 start
;;
stop)
    echo "Stopping LTS TASK_TRACKER ... "
    if [ ! -f "$LTS_PID_FILE" ]
    then
      echo "no tasktracker to stop (could not find file $LTS_PID_FILE)"
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





