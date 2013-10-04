package com.chocolatefactory.newrelic.plugins.unix;

import java.io.BufferedReader;
import java.io.IOException;
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
			try {
				if (thisCommand.getType().equals(UnixMetrics.commandTypes.MULTIDIM)) {
					metricUtils.parseMultiMetricOutput(commandName, thisMetricOutput, umetrics.allMetrics, commandReader);
					if (isDebug) {
						printMetrics(thisMetricOutput);
					} else {
						reportMetrics(thisMetricOutput);				
					}
				} else if (thisCommand.getType().equals(UnixMetrics.commandTypes.SINGLEDIM)) {
					simpleMetricOutput = metricUtils.parseOnePerLineMetricOutput(commandName, commandReader);
					if (isDebug) {
						printMetricsSimple(simpleMetricOutput);
					} else {
						reportMetricsSimple(simpleMetricOutput);				
					}
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	public void printMetrics(HashMap<String, MetricOutput> outputMetrics) {
		Iterator<String> outputIterator = outputMetrics.keySet().iterator();  
		   
		while (outputIterator.hasNext()) {  
		   String thisKey = outputIterator.next().toString();  
		   MetricOutput thisMetric = outputMetrics.get(thisKey);
		   MetricDetail thisMetricDetail = thisMetric.getMetricDetail();
		   if (thisMetric.getNamePrefix().isEmpty()) {
			   System.out.println(mungeString(thisMetricDetail.getPrefix(), thisMetricDetail.getName()) +
					   ", " + thisMetric.getValue() + " " + thisMetricDetail.getUnits());
		   } else {
			   System.out.println(mungeString(thisMetricDetail.getPrefix(), mungeString(thisMetric.getNamePrefix(), thisMetricDetail.getName())) +
					   ", " + thisMetric.getValue() + " " + thisMetricDetail.getUnits());
		   }
		   
		}
	}
	
	public String mungeString(String str1, String str2) {
		return str1 + "/" + str2;
	}

	public void printMetricsSimple(HashMap<String, Number> outputMetrics) {
		Iterator<Entry<String, Number>> outputIterator = outputMetrics.entrySet().iterator();  
		while (outputIterator.hasNext()) { 
			Map.Entry<String, Number> pairs = outputIterator.next();
			System.out.println(pairs.getKey() + ", " + getMetricType(pairs.getKey()) + ", " + pairs.getValue());   
		}
	}
	
	public void reportMetrics(HashMap<String, MetricOutput> thisMetricOutput) {
		for(MetricOutput jaun : thisMetricOutput.values()) {
			MetricDetail jaunDetail = jaun.getMetricDetail();
			
			if (jaun.getNamePrefix().isEmpty()) {
				reportMetric(mungeString(jaunDetail.getPrefix(), jaunDetail.getName()), 
					jaunDetail.getUnits(), jaun.getValue());	
			} else {
				reportMetric(mungeString(mungeString(jaunDetail.getPrefix(), jaun.getNamePrefix()), jaunDetail.getName()),
					jaunDetail.getUnits(), jaun.getValue());
			}
		}
	}
	
	public void reportMetricsSimple(HashMap<String, Number> outputMetrics) {
		Iterator<Entry<String, Number>> outputIterator = outputMetrics.entrySet().iterator();  
		while (outputIterator.hasNext()) { 
			Map.Entry<String, Number> pairs = outputIterator.next();
			String metricType = getMetricType(pairs.getKey());
			reportMetric(pairs.getKey(), metricType, pairs.getValue());
		}
	}
	
	public String getMetricType(String metricInput) {
		if (metricInput.contains("percentage")) {
			return "%";
		} else {
			for(String thisKeyword : metricInput.split("\\s")) {
				if (thisKeyword.endsWith("s")) {
					return thisKeyword;
				}
			}
		}
		return "ms";
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
}
