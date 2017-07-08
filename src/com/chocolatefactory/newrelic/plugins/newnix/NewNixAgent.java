package com.chocolatefactory.newrelic.plugins.newnix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;

import com.chocolatefactory.newrelic.plugins.newnix.utils.NewCommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.newnix.utils.NewMetricOutput;
import com.chocolatefactory.newrelic.plugins.newnix.utils.NewNixConstants;
import com.newrelic.metrics.publish.util.Logger;
import com.newrelic.metrics.publish.Agent;

public class NewNixAgent extends Agent {
    
	// Required for New Relic Plugins
	public static final String kAgentVersion = "4.0.0";
	public static final String kAgentGuid = "com.chocolatefactory.newrelic.plugins.unix";
			
	boolean isDebug, isCheckAllRegex;
	int lineLimit;
	HashMap<String, NewMetricOutput> metricOutput = new HashMap<String, NewMetricOutput>();
	HashMap<String, Object> instanceConfig = new HashMap<String, Object>();		
	HashSet<String> members;
	HashMap<String, JSONArray> mappings;
	HashMap<String, Object> metrics;
	String commandCom, commandName, commandType, 
		   hostName,
		   disksCommand, disksRegex, 
		   interfacesCommand, interfacesRegex,
		   pageSizeCommand;
	int pageSize;

	// COMPLEXDIM: multiple metrics per line, can have words in value lines
	// MULTIDIM: multiple metrics per line, can only have numbers (or dashes) in line
	// SINGLEDIM: single metric per line (usually "name value")
	static enum commandTypes{REGEXLISTDIM, REGEXDIM, SIMPLEDIM};
		
	private static final Logger logger = Logger.getLogger(NewNixAgent.class);
	
	@SuppressWarnings("unchecked")
	public NewNixAgent(HashMap<String, Object> instance) {
		super(kAgentGuid, kAgentVersion);
		instanceConfig = (HashMap<String, Object>)instance.get("agent");
		hostName = (String)instance.get("hostname");
		isDebug = instance.containsKey("debug") ? (Boolean)instance.get("debug") : false;
		disksCommand = (String)instance.get("disksCommand");
		disksRegex = (String)instance.get("disksRegex");
		interfacesCommand = (String)instance.get("interfacesCommand");
		interfacesRegex = (String)instance.get("interfacesRegex");
		pageSizeCommand = (String)instance.get("pageSizeCommand");
		pageSize = (int)instance.get("pageSize");
		
		commandCom = (String)instanceConfig.get("command");
		commandName = (String)instanceConfig.get("name");
		commandType = (String)instanceConfig.get("type");
		mappings = (HashMap<String, JSONArray>)instanceConfig.get("mappings");
		metrics = (HashMap<String, Object>)instanceConfig.get("metrics");
		isCheckAllRegex = instanceConfig.containsKey("checkAllRegex") ? (Boolean)instanceConfig.get("checkAllRegex") : false;
		lineLimit = (int) (instanceConfig.containsKey("lineLimit") ? (long) instanceConfig.get("lineLimit") : NewNixConstants.kDefaultLineLimit);
				
		if(metrics != null) {
			updateRatios();
		}
		
		logger.debug("Instance Configuration:" +
				"\ncommand: " + commandName +
				"\ndebug: " + isDebug +
				"\nhostname: " + hostName);
		
		if(commandType.equals("REGEXLISTDIM")) {
			if (commandCom.startsWith("iostat")) {
				logger.debug("Running " + disksCommand + " to get list of disks.");
				Pattern diskPattern = Pattern.compile(disksRegex);
				getMembers(disksCommand, diskPattern);
			} else {
				logger.debug("Running " + interfacesCommand + " to get list of interfaces.");
				Pattern interfacePattern = Pattern.compile(interfacesRegex);
				getMembers(interfacesCommand, interfacePattern);
			}
		}
		
	}

	@Override
    public String getAgentName() {
		return hostName;
    }
    
	@Override
	public void pollCycle() {
		ArrayList<String> commandReader = null;
		try {
			if (commandType.equals("REGEXLISTDIM")) {
				for(String thismember : members) {
					commandReader = NewCommandMetricUtils.executeCommand(
							commandCom.replace(NewNixConstants.kMemberPlaceholder, thismember));
					NewCommandMetricUtils.parseRegexMetricOutput(
						commandName, mappings, thismember, metrics, 
						lineLimit, isCheckAllRegex,
						metricOutput, commandReader);
				}
				reportMetrics();
			} else if (commandType.equals("REGEXDIM")) {
				commandReader = NewCommandMetricUtils.executeCommand(commandCom);
				NewCommandMetricUtils.parseRegexMetricOutput(
					commandName, mappings, "", metrics, 
					lineLimit, isCheckAllRegex,
					metricOutput, commandReader);
				NewCommandMetricUtils.addSummaryMetrics(metricOutput);
				reportMetrics();
			} else if (commandType.equals("SIMPLEDIM")) {
				commandReader = NewCommandMetricUtils.executeCommand(commandCom);
				reportMetricsSimple(NewCommandMetricUtils.parseSimpleMetricOutput(commandName, commandReader));
			} else {
				logger.error("Command Type " + commandType + " is invalid.");
				return;
			}
		} catch (Exception e) {
			logger.error("Error: Parsing of " + commandCom + " could not be completed.");
			logger.error(e.toString());
		}
	}
	
	public void reportMetrics() {
		if(isDebug) {
			logger.info("Debug enabled, no metrics will be sent.");
		}
		
		for(String thisMetricKey : metricOutput.keySet()) {
			NewMetricOutput thisMetric = metricOutput.get(thisMetricKey);
			// Only report current metrics. Stale metrics will be cleaned out afterward.
			if (thisMetric.isCurrent()) {
				logger.debug(thisMetricKey + ", " + thisMetric.getValue() + " " + thisMetric.getUnits());
				if(!isDebug) {
					reportMetric(thisMetricKey, thisMetric.getUnits(), thisMetric.getValue());
				}
			}
		}
		
		// After metrics are all reported, reset "current" list and clear out stale metrics
		metricOutput = NewCommandMetricUtils.resetCurrentMetrics(metricOutput);
	}
	
	public void getMembers(String command, Pattern memberPattern) {
		ArrayList<String> membersReader = NewCommandMetricUtils.executeCommand(command);
		this.members = new HashSet<String>();
		try {
			for (String line : membersReader) {
				Matcher lineMatch = memberPattern.matcher(line);
				if (lineMatch.matches()) {
					members.add(lineMatch.group(1));
				}
			}
		} catch (Exception e) {
			logger.error("Error: Parsing of " + command + "could not be completed.");
			e.printStackTrace();
		}
		logger.debug("Members found: " + members);
	}
	
	public void getPageSize() {
		if(!(pageSize > 1)) {
			for(String line : NewCommandMetricUtils.executeCommand(pageSizeCommand)) {
				try {
					pageSize = Integer.parseInt(line.trim());
					break;
				} catch (NumberFormatException e) { 
					pageSize = 1;
				}
			}
		}
	}
	
	public void reportMetricsSimple(HashMap<String, Number> outputMetrics) {
		if(isDebug) {
			logger.info("Debug enabled, no metrics will be sent.");
		}
		Iterator<Entry<String, Number>> outputIterator = outputMetrics.entrySet().iterator();  
		while (outputIterator.hasNext()) { 
			Map.Entry<String, Number> pairs = outputIterator.next();
			String metricType = NewCommandMetricUtils.getSimpleMetricType(
				pairs.getKey().substring(pairs.getKey().lastIndexOf("/") + 1));
			logger.debug(pairs.getKey() + ", " + pairs.getValue() + " " + metricType);
			if(!isDebug) {
				reportMetric(pairs.getKey(), metricType, pairs.getValue());
			}
		}
	}
	
	public void updateRatios() {
		for(String metricStr : metrics.keySet()) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> metric = (HashMap<String, Object>)metrics.get(metricStr);
			Object robj = metric.containsKey("ratio") ? metric.get("ratio") : (int) 1;
			if(robj instanceof Number) {
		        metric.put("ratio", (Number) robj);
			} else if (robj instanceof String) {
				try {
			        double rdub = Double.parseDouble((String)robj);
			        metric.put("ratio", rdub);
			    } catch(NumberFormatException ex) {
			    	try {
			    		double rdub = Double.parseDouble(((String)robj).replace(NewNixConstants.kColumnPageSize, "" + pageSize));
				        metric.put("ratio", rdub);
			    	} catch(NumberFormatException eex) {
			    		logger.error("Could not cast ratio for " + metricStr + "from what's in the config file: " + (String)robj);
			    		logger.error("Error Message:\n" +  eex.toString());
			    	}
			    }
			}	
		}
	}
}
