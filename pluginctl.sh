#!/bin/sh
# chkconfig: 2345 20 80
# description: Unix system plugin for New Relic
# processname: NR Unix Agent

### Set these as appropriate

PLUGIN_PATH=/opt/newrelic/newrelic_unix_plugin

# AIX:
# PLUGIN_JAVA_HOME=/usr/java6
# LINUX & SOLARIS:
PLUGIN_JAVA_HOME=/usr

# Change to false if you want to append to existing logs.
DELETE_LOGS_ON_STARTUP=true

### Do not change these unless instructed!

PLUGIN_NAME="New Relic Unix Plugin"
PLUGIN_LOG_FILE=$PLUGIN_PATH/logs/plugin.log
PLUGIN_PID_FILE=$PLUGIN_PATH/logs/plugin.pid
PLUGIN_JAVA_CLASS=com.chocolatefactory.newrelic.plugins.unix.Main
PLUGIN_JAVA_OPTS="-Xms16m -Xmx128m -cp $PLUGIN_PATH/bin/newrelic_unix_plugin.jar:$PLUGIN_PATH/lib/metrics_publish-2.0.1.jar:$PLUGIN_PATH/lib/json-simple-1.1.1.jar"
PLUGIN_RESTART_ON_START=0

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
	check_plugin_status
	procstatus=$?
	if [ "$procstatus" -eq 1 ] && [ -f $PLUGIN_PID_FILE ]; then
	    echo "Stopping $PLUGIN_NAME"
	    PID=`cat $PLUGIN_PID_FILE`
        kill -9 $PID
        echo "$PLUGIN_NAME running with PID $PID stopped"
		rm -f $PLUGIN_PID_FILE
    else
        echo "$PLUGIN_NAME is not running or $PLUGIN_PID_FILE not found"
    fi
}

start_plugin() {
	mkdir -p $PLUGIN_PATH/logs
	check_plugin_status
	procstatus=$?
	if [ "$procstatus" -eq 1 ]; then
		if [ $PLUGIN_RESTART_ON_START -eq 0 ]; then
			echo "Plugin is already running, restart will not occur"
			exit 2
		elif [ $PLUGIN_RESTART_ON_START -eq 1 ]; then
			echo "Restarting $PLUGIN_NAME"
			stop_plugin
		else
			echo "Plugin is already running, restart will not occur"
			exit 2
		fi
	fi

    if [ "$DELETE_LOGS_ON_STARTUP" = true ] ; then
        echo "Deleting logs"
        rm -f $PLUGIN_LOG_FILE
        rm -f $PLUGIN_PATH/logs/*.log
    fi

	echo "Starting $PLUGIN_NAME"
	nohup $PLUGIN_JAVA_HOME/bin/java $PLUGIN_JAVA_OPTS $PLUGIN_JAVA_CLASS > $PLUGIN_LOG_FILE 2>&1 &
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
stopremlogs)
	stop_plugin
	echo "Clearing plugin logs"
	rm -rf $PLUGIN_PATH/logs/*
	;;
*)
    echo "Usage: $0 [status|start|stop|stopremlogs|restart]"
    exit 1
esac
