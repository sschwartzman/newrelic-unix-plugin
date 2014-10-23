package com.chocolatefactory.newrelic.plugins.unix;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.chocolatefactory.newrelic.plugins.unix.UnixMetrics.*;
import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricOutput;
import com.newrelic.metrics.publish.util.Logger;
import com.newrelic.metrics.publish.Agent;

public class UnixAgent extends Agent {
    
	// Required for New Relic Plugins
	public static final String kAgentVersion = "0.2";
	public static final String kAgentGuid = "com.chocolatefactory.newrelic.plugins.unix";
			
	CommandMetricUtils metricUtils;
	boolean isDebug = false;
	UnixMetrics umetrics;
	HashMap<String, MetricOutput> thisMetricOutput = new HashMap<String, MetricOutput>();
	HashMap<String, Number> simpleMetricOutput = new HashMap<String, Number>();
	String commandName;
	UnixCommand thisCommand;
	private static final Logger logger = Logger.getLogger(UnixAgent.class);
	
	public UnixAgent(String os, String command, Boolean debug) {
		super(kAgentGuid, kAgentVersion);
		commandName = command;
		
		if(os.contains("linux")) {
			umetrics = new LinuxMetrics();
		} else if (os.contains("aix")) {
			umetrics = new AIXMetrics();
		} else if (os.contains("sunos")) {
			umetrics = new SolarisMetrics();
		} else {
			logger.error("Unix Agent could not detect an OS version that it supports.");
			logger.error("OS detected: " + os);
			return;
		}
		
		metricUtils = new CommandMetricUtils();
		isDebug = debug;
		if (umetrics.allCommands.containsKey(command)) {
			thisCommand = umetrics.allCommands.get(command);
		} else {
			logger.error("Unix Agent does not support this command for your OS: "+ commandName);
			thisCommand = null;
		}
	}

	@Override
    public String getComponentHumanLabel() {
		try {
			return java.net.InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			logger.debug("Naming failed: " + e.toString());
			return "testserver";
		}
    }
    
	@Override
	public void pollCycle() {
		if (thisCommand == null) {
			logger.error("Unix Agent does not support this command for your OS: "+ commandName);
			return;
		}
		
		BufferedReader commandReader = metricUtils.executeCommand(thisCommand.getCommand(), false);
		if (commandReader == null) {
			logger.error("Error: Command response is null. No result processing attempted.");
			return;
		} 
		
		// public void parseSingleLineMetricOutput(String thisCommand, HashMap<String, String[]> lineMappings, int lineLimit, HashMap<String, MetricOutput> currentMetrics, 
			// HashMap<String, MetricDetail> metricDeets, BufferedReader commandOutput) throws Exception {
		
		try {
			if (thisCommand.getType().equals(UnixMetrics.commandTypes.COMPLEXDIM)) {
				metricUtils.parseComplexMetricOutput(commandName, thisCommand.getSkipColumns(), thisMetricOutput, umetrics.allMetrics, commandReader);
				if (isDebug) {
					metricUtils.printMetrics(thisMetricOutput);
				} else {
					reportMetrics(thisMetricOutput);				
				}
			} else if (thisCommand.getType().equals(UnixMetrics.commandTypes.MULTIDIM)) {
				metricUtils.parseMultiMetricOutput(commandName, thisCommand.getSkipColumns(), thisMetricOutput, umetrics.allMetrics, commandReader);
				if (isDebug) {
					metricUtils.printMetrics(thisMetricOutput);
				} else {
					reportMetrics(thisMetricOutput);				
				}
			} else if (thisCommand.getType().equals(UnixMetrics.commandTypes.SIMPLEMULTIDIM)) {
				simpleMetricOutput = metricUtils.parseSimpleMetricOutput(commandName, commandReader);
				if (isDebug) {
					metricUtils.printMetricsSimple(simpleMetricOutput);
				} else {
					reportMetricsSimple(simpleMetricOutput);				
				}
			} else if (thisCommand.getType().equals(UnixMetrics.commandTypes.SINGLELINEDIM)) {
				metricUtils.parseSingleLineMetricOutput(commandName, thisCommand.getLineMappings(), thisCommand.getLineLimit(), thisMetricOutput, umetrics.allMetrics, commandReader);
				if (isDebug) {
					metricUtils.printMetrics(thisMetricOutput);
				} else {
					reportMetrics(thisMetricOutput);				
				}
			} else {
				logger.error("Command Type " + thisCommand.getType() + " is invalid.");
				return;
			}
		} catch (Exception e) {
			logger.error("Error: Parsing of " + thisCommand.getCommand() + "could not be completed.");
			e.printStackTrace();
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
			String metricType = metricUtils.getSimpleMetricType(pairs.getKey());
			reportMetric(pairs.getKey(), metricType, pairs.getValue());
		}
	}
}
