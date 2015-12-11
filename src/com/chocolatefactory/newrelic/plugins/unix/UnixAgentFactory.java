package com.chocolatefactory.newrelic.plugins.unix;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.Config;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.util.Logger;

public class UnixAgentFactory extends AgentFactory {
	
	String os, command, hostname;
	Boolean debug;
	Map<String, Object> global_properties;
	
	public UnixAgentFactory() {
		super();
		Logger logger = Logger.getLogger(UnixAgentFactory.class);
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

	}

	@Override
	public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
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
		
		if(properties.containsKey("hostname")) {
			hostname = ((String) properties.get("hostname"));
		} else if (global_properties.containsKey("hostname")) {
				hostname = ((String) global_properties.get("hostname"));
		} else {
			hostname = "auto";
		}
		
		command = ((String) properties.get("command"));
		
    	return new UnixAgent(os, command, debug, hostname);
	}
}