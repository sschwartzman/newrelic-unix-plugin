#!/bin/sh
PLUGIN_JAVA_HOME=/usr/java6
nohup $PLUGIN_JAVA_HOME/bin/java -cp bin/newrelic_unix_plugin.jar:lib/metrics_publish-1.2.2.jar:lib/config-1.0.1.jar:lib/json-simple-1.1.1.jar com.chocolatefactory.newrelic.plugins.unix.Main > plugin.log 2>&1 &
echo $! > plugin.pid
