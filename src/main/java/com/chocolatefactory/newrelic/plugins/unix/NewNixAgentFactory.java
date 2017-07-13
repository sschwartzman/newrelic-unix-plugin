package com.chocolatefactory.newrelic.plugins.unix;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.Config;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.util.Logger;

public class NewNixAgentFactory extends AgentFactory {
	
	private static final String kDefaultServerName = "unixserver";
	Boolean debug;
	Map<String, Object> global_properties;
	private static final Logger logger = Logger.getLogger(NewNixAgentFactory.class);
	HashMap<String, Object> agentInstanceConfigs;
	
	public NewNixAgentFactory() {
		super();
		logger.info("Unix Agent version: " + NewNixAgent.kAgentVersion);
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
		
		String os, hostname;
		
		// Setting agent instance configurations based on plugin.json
		// NOTE: Per-instance properties take precedence over global properties
		
		if (properties.containsKey("debug")) {
			debug = (Boolean) properties.get("debug");
		} else if (global_properties.containsKey("debug")) {
			debug = (Boolean) global_properties.get("debug");
		} else {
			debug = false;
		}
		
		if (properties.containsKey("OS") && !((String) properties.get("OS")).toLowerCase().equals("auto")) {
			os = ((String) properties.get("OS")).toLowerCase();
		} else if (global_properties.containsKey("OS") && !((String) global_properties.get("OS")).toLowerCase().equals("auto")) {
			os = ((String) global_properties.get("OS")).toLowerCase();
		} else {
			os = System.getProperty("os.name").toLowerCase();
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
		
		logger.info("Host OS: " + os);
		logger.info("Hostname: " + hostname);
		logger.info("Command Name: " + (String)properties.get("name") + ", Command: " + (String)properties.get("command"));
		
		agentInstanceConfigs.put("agent", properties);
		agentInstanceConfigs.put("os", os);
		agentInstanceConfigs.put("debug", debug);
		agentInstanceConfigs.put("hostname", hostname);
		
		agentInstanceConfigs.put("disksCommand", (global_properties.containsKey("disksCommand") ? (String) global_properties.get("disksCommand") : ""));
		agentInstanceConfigs.put("disksRegex", (global_properties.containsKey("disksRegex") ? (String) global_properties.get("disksRegex") : ""));
		agentInstanceConfigs.put("interfacesCommand", (global_properties.containsKey("interfacesCommand") ? (String) global_properties.get("interfacesCommand") : ""));
		agentInstanceConfigs.put("interfacesRegex", (global_properties.containsKey("interfacesRegex") ? (String) global_properties.get("interfacesRegex") : ""));
		agentInstanceConfigs.put("pageSizeCommand", (global_properties.containsKey("pageSizeCommand") ? (String) global_properties.get("pageSizeCommand") : NewNixConstants.kDefaultPagesizeCommand));
		agentInstanceConfigs.put("pageSize", (global_properties.containsKey("pageSize") ? ((Long)global_properties.get("pageSize")).intValue() : 1));
		
    	return new NewNixAgent(agentInstanceConfigs);
	}
}