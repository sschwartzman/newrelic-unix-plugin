package com.chocolatefactory.newrelic.plugins.unix;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.Config;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.util.Logger;

public class UnixAgentFactory extends AgentFactory {
	
	private static final String kDefaultServerName = "unixserver";
	Boolean debug;
	Map<String, Object> global_properties;
	private static final Logger logger = Logger.getLogger(UnixAgentFactory.class);
	UnixMetrics umetrics;
	HashMap<String, Object> agentInstanceConfigs;
	
	public UnixAgentFactory() {
		super();
		logger.info("Unix Agent version: " + UnixAgent.kAgentVersion);
		global_properties = Config.getValue("global");
		if (global_properties != null) {
			logger.debug("Global configurations found in plugin.json.");
		} else {
			logger.debug("No global configurations found in plugin.json." +
					"\nYou're probably using an old OR customized version of plugin.json." +
					"\nEither of which is OK!");
			global_properties = new HashMap<String, Object>();
		}
		agentInstanceConfigs = new HashMap<String, Object>();
		
	}

	@Override
	public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
		
		String os, command, hostname, iregex;
		String[] dcommand, icommand;
		
		if (properties.containsKey("debug")) {
			debug = (Boolean) properties.get("debug");
		}
		
		// Per-instance properties take precedence over global properties
		if (properties.containsKey("OS") && !((String) properties.get("OS")).toLowerCase().equals("auto")) {
			os = ((String) properties.get("OS")).toLowerCase();
		} else if (global_properties.containsKey("OS") && !((String) global_properties.get("OS")).toLowerCase().equals("auto")) {
			os = ((String) global_properties.get("OS")).toLowerCase();
		} else {
			os = System.getProperty("os.name").toLowerCase();
		}
		
		if (properties.containsKey("debug")) {
			debug = (Boolean) properties.get("debug");
		} else if (global_properties.containsKey("debug")) {
			debug = (Boolean) global_properties.get("debug");
		} else {
			debug = false;
		}
		
		if(properties.containsKey("hostname") && !((String)properties.get("hostname")).toLowerCase().equals("auto")) {
			hostname = ((String) properties.get("hostname"));
		} else if (global_properties.containsKey("hostname") && !((String)global_properties.get("hostname")).toLowerCase().equals("auto")) {
			hostname = ((String) global_properties.get("hostname"));
		} else {
			try {
				hostname = java.net.InetAddress.getLocalHost().getHostName(); 
			} catch (Exception e) {
				logger.error("Naming failed: " + e.toString());
				logger.error("Applying default server name (" + kDefaultServerName + ") to this server");
				hostname = kDefaultServerName;
			}
		}
		
		command = ((String) properties.get("command"));
		
		if(os.contains("linux")) {
			umetrics = new LinuxMetrics();
			icommand = new String[]{"ip","link","show"};
			iregex = "\\d+:\\s+(\\w+\\d*):.*";
		} else if (os.contains("aix")) {
			umetrics = new AIXMetrics();
			icommand = new String[]{"/usr/sbin/ifconfig", "-a"};
			iregex = "(\\w+\\d*):\\s+flags.*.*";
		} else if (os.contains("sunos")) {
			umetrics = new SolarisMetrics();
			icommand = new String[]{"/usr/sbin/ifconfig", "-a"};
			iregex = "(\\w+\\d*):\\d*:*\\s+flags.*";
		} else if (os.toLowerCase().contains("os x") || os.toLowerCase().contains("osx")) {
			umetrics = new OSXMetrics();
			dcommand = new String[]{"diskutil", "list"};
			agentInstanceConfigs.put("dcommand", dcommand);
			icommand = new String[]{"ifconfig", "-a"};
			iregex = "(\\w+\\d*):\\s+flags.*";
		} else {
			logger.error("Unix Agent could not detect an OS version that it supports.");
			logger.error("OS detected: " + os);
			return null;
		}
		
		agentInstanceConfigs.put("os", os);
		agentInstanceConfigs.put("command", command);
		agentInstanceConfigs.put("debug", debug);
		agentInstanceConfigs.put("hostname", hostname);
		agentInstanceConfigs.put("icommand", icommand);
		agentInstanceConfigs.put("iregex", iregex);
		
    	return new UnixAgent(umetrics, agentInstanceConfigs);
	}
}