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
	public static final String kAgentVersion = "3.6.3";
	public static final String kAgentGuid = "com.chocolatefactory.newrelic.plugins.unix";
			
	boolean isDebug = false;
	UnixMetrics unixMetrics;
	HashMap<String, MetricOutput> metricOutput = new HashMap<String, MetricOutput>();
	String commandName;
	String hostName = "";

	HashSet<String> members;
	UnixCommand thisCommand = null;
	private static final Logger logger = Logger.getLogger(UnixAgent.class);
	
	public UnixAgent(UnixMetrics umetrics, HashMap<String, Object> instance) {
			
		super(kAgentGuid, kAgentVersion);
		
		hostName = (String)instance.get("hostname");
		commandName = (String)instance.get("command");
		isDebug = (Boolean)instance.get("debug");
		String[] diskCommand = (String[])instance.get("dcommand");
		String diskRegex = (String)instance.get("dregex");
		String[] interfaceCommand = (String[])instance.get("icommand");
		String interfaceRegex = (String)instance.get("iregex");
		
		logger.debug("Instance Configuration:" +
				"\ncommand: " + commandName +
				"\ndebug: " + isDebug +
				"\nhostname: " + hostName);
		
		unixMetrics = umetrics;
		if (unixMetrics.allCommands.containsKey(commandName)) {
			thisCommand = unixMetrics.allCommands.get(commandName);
			if(thisCommand.getType() == UnixMetrics.commandTypes.REGEXLISTDIM) {
				if (thisCommand.getCommand()[0] == "iostat") {
					logger.debug("Running " + thisCommand.getCommand().toString() + " to get list of disks.");
					Pattern diskPattern = Pattern.compile(diskRegex);
					getMembers(diskCommand, diskPattern);
				} else {
					logger.debug("Running " + thisCommand.getCommand().toString() + " to get list of interfaces.");
					Pattern interfacePattern = Pattern.compile(interfaceRegex);
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
		return hostName;
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
				for(String thismember : members) {
					commandReader = CommandMetricUtils.executeCommand(
						CommandMetricUtils.replaceInArray(thisCommand.getCommands(), 
								UnixMetrics.kMemberPlaceholder, thismember));
					CommandMetricUtils.parseRegexMetricOutput(commandName, 
						thisCommand.getLineMappings(), thismember, 
						thisCommand.getLineLimit(), thisCommand.isCheckAllRegex(),
						metricOutput, unixMetrics.allMetrics, commandReader);
				}
				reportMetrics();
				break;
			case REGEXDIM:
				commandReader = CommandMetricUtils.executeCommand(thisCommand.getCommands());
				CommandMetricUtils.parseRegexMetricOutput(commandName, 
					thisCommand.getLineMappings(), "", 
					thisCommand.getLineLimit(), thisCommand.isCheckAllRegex(),
					metricOutput, unixMetrics.allMetrics, commandReader);
				CommandMetricUtils.addSummaryMetrics(metricOutput);
				reportMetrics();
				break;
			case SIMPLEDIM:
				commandReader = CommandMetricUtils.executeCommand(thisCommand.getCommands());
				reportMetricsSimple(CommandMetricUtils.parseSimpleMetricOutput(commandName, commandReader));
				break;
			default:
				logger.error("Command Type " + thisCommand.getType().toString() + " is invalid.");
				return;
			}
		} catch (Exception e) {
			logger.error("Error: Parsing of " + Arrays.toString(thisCommand.getCommand()) + " could not be completed.");
			logger.debug(e.getMessage());
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
