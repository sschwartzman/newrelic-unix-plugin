#!/bin/sh
# chkconfig: 2345 20 80
# description: Unix system plugin for New Relic
# processname: NR Unix Agent

### Set these as appropriate

# Change to false if you want to append to existing logs.
DELETE_LOGS_ON_STARTUP=true

# Uncomment if you are using a JDK packaged with WebSphere
# USE_IBM_JSSE=true

# Manually define Plugin path if this script can't find it
# PLUGIN_PATH=/opt/newrelic/newrelic_unix_plugin

# Manually define Java path & filename if this script can't find it
# AIX:
# PLUGIN_JAVA=/usr/java6/bin/java
# LINUX, OSX & SOLARIS:
# PLUGIN_JAVA=/usr/bin/java

### Do not change these unless instructed!

# Attempt to set plugin path if not manually defined above
if [ -z "$PLUGIN_PATH" ]; then
   RELATIVE_PATH=`dirname "$0"`
   PLUGIN_PATH=`eval "cd \"$RELATIVE_PATH\" && pwd"`
fi
echo "Plugin location: $PLUGIN_PATH"

# Attempt to set Java path & filename if not manually defined above
if [ -z "$PLUGIN_JAVA" ]; then
    if [ -n "$JAVA_HOME" ]; then
        PLUGIN_JAVA=$JAVA_HOME/bin/java
    else
        PLUGIN_JAVA=`which java`
    fi
    # If attempt to set Java path & filename failed, throw error
    if [ -z "$PLUGIN_JAVA" ]; then
        echo "Could not find Java and is not manually defined."
        echo "Please define manually in pluginctl.sh"
        exit 1
    fi
fi
echo "Java location: $PLUGIN_JAVA"

PLUGIN_JAVA_VERSION=`$PLUGIN_JAVA -version 2>&1 | awk 'NR==1{ gsub(/"/,""); print $3 }'`
PLUGIN_JAVA_VERSION_FULL=`$PLUGIN_JAVA -version 2>&1`
echo "Java version: $PLUGIN_JAVA_VERSION"

PLUGIN_NAME="New Relic Unix Plugin"
PLUGIN_ERR_FILE=$PLUGIN_PATH/logs/newrelic_unix_plugin.err
PLUGIN_PID_FILE=$PLUGIN_PATH/logs/newrelic_unix_plugin.pid
PLUGIN_JAVA_CLASS=com.chocolatefactory.newrelic.plugins.unix.Main
PLUGIN_JAVA_OPTS="-Xms16m -Xmx128m -cp $PLUGIN_PATH/bin/newrelic_unix_plugin.jar:$PLUGIN_PATH/lib/metrics_publish-2.0.1.jar:$PLUGIN_PATH/lib/json-simple-1.1.1.jar"

# Added for IBM JSSE support
if [ "$USE_IBM_JSSE" == "true" ]; then
    PLUGIN_JAVA_OPTS="$PLUGIN_JAVA_OPTS -Djava.security.properties=$PLUGIN_PATH/etc/ibm_jsse.java.security"
fi

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
        rm -f $PLUGIN_ERR_FILE
        rm -f $PLUGIN_PATH/logs/*.log
    fi

	echo "Starting $PLUGIN_NAME"
    echo "Plugin location: $PLUGIN_PATH" > $PLUGIN_ERR_FILE
    echo "Java location: $PLUGIN_JAVA" >> $PLUGIN_ERR_FILE
    echo "Java version: $PLUGIN_JAVA_VERSION_FULL" >> $PLUGIN_ERR_FILE
    nohup $PLUGIN_JAVA $PLUGIN_JAVA_OPTS $PLUGIN_JAVA_CLASS >/dev/null 2>>$PLUGIN_ERR_FILE &
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
