package com.chocolatefactory.newrelic.plugins.unix;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.chocolatefactory.newrelic.plugins.unix.UnixAgentFactory;

public class Main {	
	public static void main(String[] args) {
		try {
	        Runner runner = new Runner();
	        runner.add(new UnixAgentFactory());
	        runner.setupAndRun(); // Never returns
	    } catch (ConfigurationException e) {
	        System.err.println("ERROR: " + e.getMessage());
	        System.exit(-1);
	    }
	}
}