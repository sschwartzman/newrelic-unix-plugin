package com.chocolatefactory.newrelic.plugins.unix;

import java.io.BufferedReader;
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
	public static final String kAgentVersion = "0.2";
	public static final String kAgentGuid = "com.chocolatefactory.newrelic.plugins.unix";
			
	boolean isDebug = false;
	UnixMetrics umetrics;
	HashMap<String, MetricOutput> thisMetricOutput = new HashMap<String, MetricOutput>();
	String commandName;
	String hostName;
	String[] interfaceCommand;
	HashSet<String> interfaces;
	UnixCommand thisCommand;
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
			thisCommand = null;
		}
		if (!hostname.isEmpty()) {
			hostName = hostname;
		}
	}

	@Override
    public String getAgentName() {
		if (hostName != null && !hostName.isEmpty()) {
			return hostName;
		} else {
			try {
				return java.net.InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
				logger.debug("Naming failed: " + e.toString());
				return "testserver";
			}
		}
    }
    
	@Override
	public void pollCycle() {
		BufferedReader commandReader;
		if (thisCommand == null) {
			logger.error("Unix Agent does not support this command for your OS: "+ commandName);
			return;
		}
		
		try {
			switch(thisCommand.getType()) {
			case INTERFACEDIM:
				for(String thisinterface : interfaces) {
					commandReader = this.getCommandReader(CommandMetricUtils.replaceInArray(thisCommand.getCommand(), UnixMetrics.kInterfacePlaceholder, thisinterface));
					CommandMetricUtils.parseRegexMetricOutput(commandName, thisCommand.getLineMappings(), thisinterface, thisCommand.getLineLimit(), thisMetricOutput, umetrics.allMetrics, commandReader);
				}
				reportMetrics(thisMetricOutput);
				break;
			case REGEXDIM:
				commandReader = this.getCommandReader(thisCommand.getCommand());
				CommandMetricUtils.parseRegexMetricOutput(commandName, thisCommand.getLineMappings(), "", thisCommand.getLineLimit(), thisMetricOutput, umetrics.allMetrics, commandReader);
				reportMetrics(thisMetricOutput);
				break;
			case SIMPLEDIM:
				commandReader = this.getCommandReader(thisCommand.getCommand());
				reportMetricsSimple(CommandMetricUtils.parseSimpleMetricOutput(commandName, commandReader));
				break;
			default:
				logger.error("Command Type " + thisCommand.getType() + " is invalid.");
				return;
			}
		} catch (Exception e) {
			logger.error("Error: Parsing of " + thisCommand.getCommand() + "could not be completed.");
			e.printStackTrace();
		}
	}
	
	public BufferedReader getCommandReader(String[] thisCommand) { 
		BufferedReader commandReader = CommandMetricUtils.executeCommand(thisCommand, false);
		if (commandReader == null) {
			logger.error("Error: Command response is null. No result processing attempted.");
			return null;
		}
		else return commandReader;
	}
	
	public void reportMetrics(HashMap<String, MetricOutput> thisMetricOutput) {
		if(isDebug) {
			logger.info("Debug enabled, no metrics will be sent.");
		}
		for(MetricOutput thisMetric : thisMetricOutput.values()) {
			MetricDetail thisMetricDetail = thisMetric.getMetricDetail();
			logger.debug(CommandMetricUtils.mungeString(thisMetricDetail.getPrefix(), 
					CommandMetricUtils.mungeString(thisMetric.getNamePrefix(), thisMetricDetail.getName())) +
				", " + thisMetric.getValue() + " " + thisMetricDetail.getUnits());
			if(!isDebug) {
				reportMetric(CommandMetricUtils.mungeString(CommandMetricUtils.mungeString(thisMetricDetail.getPrefix(), thisMetric.getNamePrefix()), 
				thisMetricDetail.getName()), thisMetricDetail.getUnits(), thisMetric.getValue());
			}
		}
	}
	
	public void getInterfaces() {
		BufferedReader interfacesReader = this.getCommandReader(interfaceCommand);
		Pattern interfacePattern = Pattern.compile("(?!\\s+)(\\w+\\d*)[:]{0,1}\\s+.*");
		String line;
		this.interfaces = new HashSet<String>();
		try {
			while((line = interfacesReader.readLine()) != null) {
				Matcher lineMatch = interfacePattern.matcher(line);
				if (lineMatch.matches()) {
					interfaces.add(lineMatch.group(1));
				}
			}
		} catch (Exception e) {
			logger.error("Error: Parsing of " + interfaceCommand + "could not be completed.");
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
			String metricType = CommandMetricUtils.getSimpleMetricType(pairs.getKey().substring(pairs.getKey().lastIndexOf("/") + 1));
			logger.debug(pairs.getKey() + ", " + pairs.getValue() + " " + metricType);
			if(!isDebug) {
				reportMetric(pairs.getKey(), metricType, pairs.getValue());
			}
		}
	}
}
