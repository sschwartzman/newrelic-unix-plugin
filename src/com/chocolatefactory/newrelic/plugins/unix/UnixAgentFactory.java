package com.chocolatefactory.newrelic.plugins.unix;

import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

public class UnixAgentFactory extends AgentFactory {

	public UnixAgentFactory(String agentConfigFileName) {
		super(agentConfigFileName);
		// TODO Auto-generated constructor stub
	}

	public UnixAgentFactory() {
		// TODO Auto-generated constructor stub
		super("UnixPlugin.conf");
	}

	@Override
	public Agent createConfiguredAgent(Map<String, Object> properties)
			throws ConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	//public Agent createConfiguredAgent(Map<String, Object> properties)
		//	throws ConfigurationException {
		// TODO Auto-generated method stub
		// return new UnixAgent(UnixMetrics.kAgentGuid, UnixMetrics.kAgentVersion);
	//}
}