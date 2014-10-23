#!/bin/sh
# chkconfig: 2345 20 80
# description: Unix system plugin for New Relic
# processname: NR Unix Agent

### Set these as appropriate

PLUGIN_PATH=/opt/newrelic/newrelic_unix_plugin

# LINUX:
PLUGIN_JAVA_HOME=/usr
# AIX:
# PLUGIN_JAVA_HOME=/usr/java6

### Do not change these unless instructed!

PLUGIN_NAME="New Relic Unix Plugin"
PLUGIN_PID_FILE=$PLUGIN_PATH/logs/plugin.pid
PLUGIN_JAVA_CLASS=com.chocolatefactory.newrelic.plugins.unix.Main
PLUGIN_JAVA_OPTS="-cp $PLUGIN_PATH/bin/newrelic_unix_plugin.jar:$PLUGIN_PATH/lib/metrics_publish-2.0.0.jar:$PLUGIN_PATH/lib/json-simple-1.1.1.jar"
PLUGIN_RESTART_ON_START=false

check_plugin_status() {
    echo "Checking $PLUGIN_NAME"
    if [ -f $PLUGIN_PID_FILE ]; then
        PID=`cat $PLUGIN_PID_FILE`
        if [ -z "`ps -ef | grep ${PID} | grep -v grep`" ]; then
            echo "Process dead but $PLUGIN_PID_FILE exists"
			echo "Deleting $PLUGIN_PID_FILE"
			rm -f $PLUGIN_PID_FILE
			procstatus=0
        else
            echo "$PLUGIN_NAME is running with PID $PID"
			procstatus=1
        fi
    else
        echo "$PLUGIN_NAME is not running"
        procstatus=0
    fi
	return "$procstatus"
}

stop_plugin() {
    echo "Stopping $PLUGIN_NAME"
    PID=`cat $PLUGIN_PID_FILE`
    if [ -f $PLUGIN_PID_FILE ]; then
        kill -9 $PID
        echo "$PLUGIN_NAME running with PID $PID stopped"
        rm -f $PLUGIN_PID_FILE
    else
        echo "$PLUGIN_PID_FILE not found"
    fi
}

start_plugin() {
	check_plugin_status
	procstatus=$?
	if [ "$procstatus" -eq 1 ]; then
		if [ "$PLUGIN_RESTART_ON_START" -eq false ]; then
			echo "Plugin is already running, restart will not occur"
			exit 2
		elif [ "$PLUGIN_RESTART_ON_START" -eq true ]; then
			echo "Restarting $PLUGIN_NAME"
			stop_plugin
		else
			echo "Plugin is already running, restart will not occur"
			exit 2
		fi
	fi
	
	echo "Starting $PLUGIN_NAME"
	nohup $PLUGIN_JAVA_HOME/bin/java $PLUGIN_JAVA_OPTS $PLUGIN_JAVA_CLASS 2>&1 &
	PID=`echo $!`
	if [ -z $PID ]; then
    	echo "$PLUGIN_NAME failed to start"
    	exit 1
    else
        echo $PID > $PLUGIN_PID_FILE
        echo "$PLUGIN_NAME started with PID $PID"
		exit 0
    fi
}

echo ""
case "$1" in
status)
	check_plugin_status
	;;
start) 
	start_plugin
	;;
restart)
    echo "Restarting $PLUGIN_NAME"
  	stop_plugin
  	start_plugin
	;;
stop)
	stop_plugin
	;;
*)
    echo "Usage: $0 [status|start|stop|restart]"
    exit 1
esac
