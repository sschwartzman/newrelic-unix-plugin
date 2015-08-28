package com.chocolatefactory.newrelic.plugins.unix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.unix.UnixMetrics.*;
import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricOutput;
import com.newrelic.metrics.publish.util.Logger;
import com.newrelic.metrics.publish.Agent;

public class UnixAgent extends Agent {
    
	// Required for New Relic Plugins
	public static final String kAgentVersion = "3.1.2";
	public static final String kAgentGuid = "com.chocolatefactory.newrelic.plugins.unix";
	
	static final String kDefaultServerName = "unixserver";
			
	boolean isDebug = false;
	UnixMetrics umetrics;
	HashMap<String, MetricOutput> metricOutput = new HashMap<String, MetricOutput>();
	String commandName;
	String hostName = "";
	String[] interfaceCommand;
	HashSet<String> interfaces;
	UnixCommand thisCommand = null;
	private static final Logger logger = Logger.getLogger(UnixAgent.class);
	
	public UnixAgent(String os, String command, Boolean debug, String hostname) {
		super(kAgentGuid, kAgentVersion);
		commandName = command;
		if(os.contains("linux")) {
			umetrics = new LinuxMetrics();
			interfaceCommand = new String[]{"/sbin/ifconfig", "-a"};
		} else if (os.contains("aix")) {
			umetrics = new AIXMetrics();
			interfaceCommand = new String[]{"/usr/sbin/ifconfig", "-a"};
		} else if (os.contains("sunos")) {
			umetrics = new SolarisMetrics();
			interfaceCommand = new String[]{"/usr/sbin/ifconfig", "-a"};
		} else {
			logger.error("Unix Agent could not detect an OS version that it supports.");
			logger.error("OS detected: " + os);
			return;
		}
		
		isDebug = debug;
		if (umetrics.allCommands.containsKey(command)) {
			thisCommand = umetrics.allCommands.get(command);
			if(thisCommand.getType() == UnixMetrics.commandTypes.INTERFACEDIM) {
				getInterfaces();
			}
		} else {
			logger.error("Unix Agent does not support this command for your OS: "+ commandName);
		}
		
		if (!hostname.isEmpty()) {
			hostName = hostname;
		}
	}

	@Override
    public String getAgentName() {
		try {
			if (hostName != null && !hostName.isEmpty()) {
				return hostName;
			} else {
				return java.net.InetAddress.getLocalHost().getHostName();
			} 
		} catch (Exception e) {
			logger.error("Naming failed: " + e.toString());
			logger.error("Applying default server name (" + kDefaultServerName + ") to this server");
			return kDefaultServerName;
		}
    }
    
	@Override
	public void pollCycle() {
		ArrayList<String> commandReader = null;
		if (thisCommand == null) {
			logger.error("Unix Agent experienced an error initializing: " + commandName);
			return;
		}

		try {
			switch(thisCommand.getType()) {
			case INTERFACEDIM:
				for(String thisinterface : interfaces) {
					commandReader = CommandMetricUtils.executeCommand(
						CommandMetricUtils.replaceInArray(thisCommand.getCommand(), 
						UnixMetrics.kInterfacePlaceholder, thisinterface));
					CommandMetricUtils.parseRegexMetricOutput(commandName, 
						thisCommand.getLineMappings(), thisinterface, 
						thisCommand.getLineLimit(), thisCommand.isCheckAllRegex(),
						metricOutput, umetrics.allMetrics, commandReader);
				}
				reportMetrics();
				break;
			case REGEXDIM:
				commandReader = CommandMetricUtils.executeCommand(thisCommand.getCommand());
				CommandMetricUtils.parseRegexMetricOutput(commandName, 
					thisCommand.getLineMappings(), "", 
					thisCommand.getLineLimit(), thisCommand.isCheckAllRegex(),
					metricOutput, umetrics.allMetrics, commandReader);
				reportMetrics();
				break;
			case SIMPLEDIM:
				commandReader = CommandMetricUtils.executeCommand(thisCommand.getCommand());
				reportMetricsSimple(CommandMetricUtils.parseSimpleMetricOutput(commandName, commandReader));
				break;
			default:
				logger.error("Command Type " + thisCommand.getType().toString() + " is invalid.");
				return;
			}
		} catch (Exception e) {
			logger.error("Error: Parsing of " + Arrays.toString(thisCommand.getCommand()) + "could not be completed.");
			e.printStackTrace();
		}
	}
	
	public void reportMetrics() {
		if(isDebug) {
			logger.info("Debug enabled, no metrics will be sent.");
		}
		for(String thisMetricKey : metricOutput.keySet()) {
			MetricOutput thisMetric = metricOutput.get(thisMetricKey);
			// Only report current metrics. Stale metrics will be cleaned out afterward.
			if (thisMetric.isCurrent()) {
				MetricDetail thisMetricDetail = thisMetric.getMetricDetail();
				logger.debug(CommandMetricUtils.mungeString(thisMetricDetail.getPrefix(), 
						CommandMetricUtils.mungeString(thisMetric.getNamePrefix(), thisMetricDetail.getName())) +
						", " + thisMetric.getValue() + " " + thisMetricDetail.getUnits());
				if(!isDebug) {
					reportMetric(CommandMetricUtils.mungeString(
						CommandMetricUtils.mungeString(thisMetricDetail.getPrefix(), thisMetric.getNamePrefix()), 
						thisMetricDetail.getName()), thisMetricDetail.getUnits(), thisMetric.getValue());
				}
			}
		}	
		// After metrics are all reported, reset "current" list and clear out stale metrics
		metricOutput = CommandMetricUtils.resetCurrentMetrics(metricOutput);
	}
	
	public void getInterfaces() {
		ArrayList<String> interfacesReader = CommandMetricUtils.executeCommand(interfaceCommand);
		Pattern interfacePattern = Pattern.compile("(?!\\s+)(\\w+\\d*)[:]{0,1}\\s+.*");
		this.interfaces = new HashSet<String>();
		try {
			for (String line : interfacesReader) {
				Matcher lineMatch = interfacePattern.matcher(line);
				if (lineMatch.matches()) {
					interfaces.add(lineMatch.group(1));
				}
			}
		} catch (Exception e) {
			logger.error("Error: Parsing of " + Arrays.toString(interfaceCommand) + "could not be completed.");
			e.printStackTrace();
		}
		logger.debug("Interfaces found: " + interfaces);
	}
	
	public void reportMetricsSimple(HashMap<String, Number> outputMetrics) {
		if(isDebug) {
			logger.info("Debug enabled, no metrics will be sent.");
		}
		Iterator<Entry<String, Number>> outputIterator = outputMetrics.entrySet().iterator();  
		while (outputIterator.hasNext()) { 
			Map.Entry<String, Number> pairs = outputIterator.next();
			String metricType = CommandMetricUtils.getSimpleMetricType(
				pairs.getKey().substring(pairs.getKey().lastIndexOf("/") + 1));
			logger.debug(pairs.getKey() + ", " + pairs.getValue() + " " + metricType);
			if(!isDebug) {
				reportMetric(pairs.getKey(), metricType, pairs.getValue());
			}
		}
	}
}
