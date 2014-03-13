package com.chocolatefactory.newrelic.plugins.unix;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
	HashMap<String, Number> simpleMetricOutput = new HashMap<String, Number>();
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
			if (commandReader == null) {
				metricUtils.getLogger().severe("Error: Command response is null. No result processing attempted.");
			} else {
				try {
					if (thisCommand.getType().equals(UnixMetrics.commandTypes.MULTIDIM)) {
						metricUtils.parseMultiMetricOutput(commandName, thisMetricOutput, umetrics.allMetrics, commandReader, thisCommand.getSkipColumns());
						if (isDebug) {
							metricUtils.printMetrics(thisMetricOutput);
						} else {
							reportMetrics(thisMetricOutput);				
						}
					} else if (thisCommand.getType().equals(UnixMetrics.commandTypes.SINGLEDIM)) {
						simpleMetricOutput = metricUtils.parseOnePerLineMetricOutput(commandName, commandReader);
						if (isDebug) {
							metricUtils.printMetricsSimple(simpleMetricOutput);
						} else {
							reportMetricsSimple(simpleMetricOutput);				
						}
					}
					
				} catch (Exception e) {
					metricUtils.getLogger().severe("Error: Parsing of " + thisCommand.getCommand() + "could not be completed.");
					e.printStackTrace();
				}
			}
		}
	}
	
	public void reportMetrics(HashMap<String, MetricOutput> thisMetricOutput) {
		for(MetricOutput jaun : thisMetricOutput.values()) {
			MetricDetail jaunDetail = jaun.getMetricDetail();
			
			if (jaun.getNamePrefix().isEmpty()) {
				reportMetric(metricUtils.mungeString(jaunDetail.getPrefix(), jaunDetail.getName()), 
					jaunDetail.getUnits(), jaun.getValue());	
			} else {
				reportMetric(metricUtils.mungeString(metricUtils.mungeString(jaunDetail.getPrefix(), jaun.getNamePrefix()), 
					jaunDetail.getName()), jaunDetail.getUnits(), jaun.getValue());
			}
		}
	}
	
	public void reportMetricsSimple(HashMap<String, Number> outputMetrics) {
		Iterator<Entry<String, Number>> outputIterator = outputMetrics.entrySet().iterator();  
		while (outputIterator.hasNext()) { 
			Map.Entry<String, Number> pairs = outputIterator.next();
			String metricType = metricUtils.getMetricType(pairs.getKey());
			reportMetric(pairs.getKey(), metricType, pairs.getValue());
		}
	}

	@Override
	public String getComponentHumanLabel() {
		try {
			return java.net.InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			return "testserver";
		}
		
	}
}
