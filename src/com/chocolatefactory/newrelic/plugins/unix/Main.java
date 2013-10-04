package com.chocolatefactory.newrelic.plugins.unix;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

import java.io.File;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Main {
	public static void main(String[] args) throws Exception {
		Runner runner = new Runner();
	
		Config pluginConfig = ConfigFactory.parseFile(new File("config/unixplugin.config"));
		String thisOS;
		Boolean isDebug;
		
		if (pluginConfig.hasPath("OS") && !pluginConfig.getString("OS").equals("auto")) {
			thisOS = pluginConfig.getString("OS").toLowerCase();
		} else {
			thisOS = System.getProperty("os.name").toLowerCase();
		}
		
		if (pluginConfig.hasPath("debug")) {
			isDebug = pluginConfig.getBoolean("debug");
		} else {
			isDebug = false;
		}
		
		if (!pluginConfig.atPath("run").isEmpty()) {
			for (String thisCommand : pluginConfig.getStringList("run")) {
				runner.register(new UnixAgent(UnixMetrics.kAgentGuid, UnixMetrics.kAgentVersion, thisOS, thisCommand, isDebug));
			}
		} else {
			System.err.println("Error with configuration: no runners configured");
			System.exit(-1);
		}
	
		try {
			runner.setupAndRun();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.err.println("Error with starting runners");
			System.exit(-1);
		}
	}
}