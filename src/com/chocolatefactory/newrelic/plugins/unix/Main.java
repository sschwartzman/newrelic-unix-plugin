package com.chocolatefactory.newrelic.plugins.unix;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

public class Main {
	public static void main(String[] args) {
    	Runner runner = new Runner();
    	runner.register(new UnixAgent(UnixMetrics.kAgentGuid, UnixMetrics.kAgentVersion));
    	
		try {
	    	//Never returns
	    	runner.setupAndRun();
		} catch (ConfigurationException e) {
			e.printStackTrace();
    		System.err.println("Error configuring");
    		System.exit(-1);
		}
    	
    }
}