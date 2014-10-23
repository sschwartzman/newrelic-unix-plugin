package com.chocolatefactory.newrelic.plugins.unix;

import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

public class UnixAgentFactory extends AgentFactory {
	
	public UnixAgentFactory() {
		// TODO Auto-generated constructor stub
		super();
	}

	@Override
	public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
		String os, command;
		Boolean debug;

		if (properties.containsKey("OS") && !((String) properties.get("OS")).toLowerCase().equals("auto")) {
			os = ((String) properties.get("OS")).toLowerCase();
		} else {
			os = System.getProperty("os.name").toLowerCase();
		}
		command = ((String) properties.get("command"));
		
		if (properties.containsKey("debug")) {
			debug = (Boolean) properties.get("debug");
		} else {
			debug = false;
		}
		
    	return new UnixAgent(os, command, debug);
	}
}