package com.chocolatefactory.newrelic.plugins.unix;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import com.chocolatefactory.newrelic.plugins.unix.UnixMetrics.UnixCommand;
import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricOutput;
import com.newrelic.metrics.publish.Agent;

public class UnixAgent extends Agent {

	CommandMetricUtils metricUtils;
	boolean isDebug = false;
	
	UnixMetrics umetrics = new UnixMetrics();
	HashMap<String, MetricOutput> thisMetricOutput = new HashMap<String, MetricOutput>();
	String commandName, fullCommand;
	UnixCommand thisCommand;
	
	public UnixAgent(String GUID, String version, String os, String command, Boolean debug) {
		super(GUID, version);
		metricUtils = new CommandMetricUtils();
		fullCommand = umetrics.mungeString(os, command);
		commandName = command;
		isDebug = debug;
		if (umetrics.allCommands.containsKey(fullCommand)) {
			thisCommand = umetrics.allCommands.get(fullCommand);			
		} else {
			thisCommand = null;
		}

	}

	@Override
	public void pollCycle() {
		if (thisCommand != null) {
			BufferedReader commandReader = metricUtils.executeCommand(thisCommand.getCommand(), false);
			try {
				if (thisCommand.getType().equals(UnixMetrics.commandTypes.MULTIDIM)) {
					metricUtils.parseMultiMetricOutput(commandName, thisMetricOutput, umetrics.allMetrics, commandReader);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (isDebug) {
				metricUtils.printMetrics(thisMetricOutput);
			} else {
				reportMetrics(thisMetricOutput);				
			}
		}
	}

	@Override
	public String getComponentHumanLabel() {
		// TODO Auto-generated method stub
		try {
			return java.net.InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			return "testserver";
		}
		
	}
	
	public void reportMetrics(HashMap<String, MetricOutput> thisMetricOutput) {
		for(MetricOutput jaun : thisMetricOutput.values()) {
			MetricDetail jaunDetail = jaun.getMetricDetail();
			
			if (jaun.getNamePrefix().isEmpty()) {
				reportMetric(jaunDetail.getPrefix() + "/" + jaunDetail.getName(), 
					jaunDetail.getUnits(), jaun.getValue());	
			} else {
				reportMetric(jaunDetail.getPrefix() + "/" + jaun.getNamePrefix() + "/" + jaunDetail.getName(),
					jaunDetail.getUnits(), jaun.getValue());
			}
		}
	}
}
