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

import com.chocolatefactory.newrelic.plugins.unix.UnixMetrics;
import com.chocolatefactory.newrelic.plugins.unix.UnixMetrics.UnixCommand;
import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricOutput;
import com.newrelic.metrics.publish.util.Logger;
import com.newrelic.metrics.publish.Agent;

public class UnixAgent extends Agent {
    
	// Required for New Relic Plugins
	public static final String kAgentVersion = "3.4";
	public static final String kAgentGuid = "com.chocolatefactory.newrelic.plugins.unix";
	
	static final String kDefaultServerName = "unixserver";
			
	boolean isDebug = false;
	UnixMetrics umetrics;
	HashMap<String, MetricOutput> metricOutput = new HashMap<String, MetricOutput>();
	String commandName;
	String hostName = "";
	String[] interfaceCommand, diskCommand;
	HashSet<String> members;
	UnixCommand thisCommand = null;
	private static final Logger logger = Logger.getLogger(UnixAgent.class);
	
	public UnixAgent(String os, String command, Boolean debug, String hostname) {
			
		super(kAgentGuid, kAgentVersion);
		
		hostName = hostname;
		commandName = command;
		isDebug = debug;
		
		if (hostname == null || hostname.isEmpty() || hostname.equals("auto")) {
			try {
				hostName = java.net.InetAddress.getLocalHost().getHostName(); 
			} catch (Exception e) {
				logger.error("Naming failed: " + e.toString());
				logger.error("Applying default server name (" + kDefaultServerName + ") to this server");
				hostName = kDefaultServerName;
			}
		} else {
			hostName = hostname;
		}
		
		logger.debug("Instance Configuration:" +
				"\nOS: " + os +
				"\ncommand: " + commandName +
				"\ndebug: " + isDebug +
				"\nhostname: " + hostName);
		
		if(os.contains("linux")) {
			umetrics = new LinuxMetrics();
			interfaceCommand = new String[]{"/sbin/ifconfig", "-a"};
		} else if (os.contains("aix")) {
			umetrics = new AIXMetrics();
			interfaceCommand = new String[]{"/usr/sbin/ifconfig", "-a"};
		} else if (os.contains("sunos")) {
			umetrics = new SolarisMetrics();
			interfaceCommand = new String[]{"/usr/sbin/ifconfig", "-a"};
		} else if (os.toLowerCase().contains("os x") || os.toLowerCase().contains("osx")) {
			umetrics = new OSXMetrics();
			interfaceCommand = new String[]{"ifconfig", "-a"};
			diskCommand = new String[]{"diskutil", "list"};
		} else {
			logger.error("Unix Agent could not detect an OS version that it supports.");
			logger.error("OS detected: " + os);
			return;
		}
		
		if (umetrics.allCommands.containsKey(command)) {
			thisCommand = umetrics.allCommands.get(command);
			if(thisCommand.getType() == UnixMetrics.commandTypes.REGEXLISTDIM) {
				if (thisCommand.getCommand()[0] == "iostat") {
					Pattern diskPattern = Pattern.compile("\\/dev\\/(\\w+\\d*)\\s+\\([\\w\\s,]+\\):.*");
					getMembers(diskCommand, diskPattern);
				} else {
					Pattern interfacePattern = Pattern.compile("(?!\\s+)(\\w+\\d*)[:]{0,1}\\s+.*");
					getMembers(interfaceCommand, interfacePattern);
				}
			}
		} else {
			logger.error("Unix Agent does not support this command for your OS: "+ commandName);
			return;
		}
	}

	@Override
    public String getAgentName() {
		try {
			if (hostName == null || hostName.isEmpty() || hostName.equals("auto"))
				hostName = java.net.InetAddress.getLocalHost().getHostName(); 
			return hostName; 
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
			case REGEXLISTDIM:
				String replacemember = UnixMetrics.kMemberPlaceholder;
				for(String thismember : members) {
					commandReader = CommandMetricUtils.executeCommand(
						CommandMetricUtils.replaceInArray(thisCommand.getCommand(), 
							replacemember, thismember));
					CommandMetricUtils.parseRegexMetricOutput(commandName, 
						thisCommand.getLineMappings(), thismember, 
						thisCommand.getLineLimit(), thisCommand.isCheckAllRegex(),
						metricOutput, umetrics.allMetrics, commandReader);
					replacemember = thismember;
				}
				CommandMetricUtils.replaceInArray(thisCommand.getCommand(), 
					replacemember, UnixMetrics.kMemberPlaceholder);
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
	
	public void getMembers(String[] command, Pattern memberPattern) {
		ArrayList<String> membersReader = CommandMetricUtils.executeCommand(command);

		this.members = new HashSet<String>();
		try {
			for (String line : membersReader) {
				Matcher lineMatch = memberPattern.matcher(line);
				if (lineMatch.matches()) {
					members.add(lineMatch.group(1));
				}
			}
		} catch (Exception e) {
			logger.error("Error: Parsing of " + Arrays.toString(command) + "could not be completed.");
			e.printStackTrace();
		}
		logger.debug("Members found: " + members);
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
