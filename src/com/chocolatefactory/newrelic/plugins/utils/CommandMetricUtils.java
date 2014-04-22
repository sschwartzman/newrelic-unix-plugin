package com.chocolatefactory.newrelic.plugins.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.newrelic.metrics.publish.binding.Context;

public class CommandMetricUtils {

	private Runtime rt;
	private Logger logger;
	
	private Pattern dashesPattern = Pattern.compile("\\s*[\\w-]+(\\s+[-]+)+(\\s[\\w-]*)*");
	private Pattern singleMetricLinePattern = Pattern.compile("\\S*(\\d+)\\s+([\\w-%\\(\\)])(\\s{0,1}[\\w-%\\(\\)])*");
	private Pattern multiHeaderLinePattern = Pattern.compile("\\s*[\\w-%]+(\\s+[\\w-%]+)+");
	private Pattern multiValueLinePattern = Pattern.compile("\\s*[\\d.]+(\\s+[\\d.]+)+");
	private Pattern complexHeaderLinePattern = Pattern.compile("\\s*[\\w-_%:]+(\\s+[\\w-_%]+)*");
	private Pattern complexValueLinePattern = Pattern.compile("\\s*[\\w-_:]*(\\s+[\\d.-]+[%]*)+(\\s+[\\w-_]*)*");
	
	int BUFFER_SIZE = 1000;
	
	public CommandMetricUtils() {
		setLogger(Context.getLogger());
		rt = Runtime.getRuntime();		
	}
		
	public BufferedReader executeCommand(String[] command, Boolean useFile) {
		BufferedReader br = null;
		if(useFile) {
			File commandFile = new File(command + ".out");
			getLogger().finer("Opening file: " + commandFile.getAbsolutePath());
			if (!commandFile.exists()) {
				getLogger().severe("Error: " + commandFile.getAbsolutePath() + " does not exist.");
			} else if(!commandFile.isFile()) {
				getLogger().severe("Error: " + commandFile.getAbsolutePath() + " is not a file.");
			} else {
				try {
					br = new BufferedReader(new FileReader(commandFile));
				} catch (Exception e) {
					getLogger().severe("Error: " + commandFile.getAbsolutePath() + " does not exist.");
					e.printStackTrace();
					br = null;
				}
			}
		} else {
			Process proc;
			try {
				if (command != null) {
					getLogger().finer("Begin execution of " + Arrays.toString(command));
					proc = rt.exec(command);
					br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				} else {
					getLogger().severe("Error: command was null.");
				}
			} catch (Exception e) {
				getLogger().severe("Error: Execution of " + Arrays.toString(command) + " failed.");
				e.printStackTrace();
				br = null;
			}
		}
		
		return br;
	}
	
	public void parseComplexMetricOutput(String thisCommand, HashMap<String, MetricOutput> currentMetrics, HashMap<String,MetricDetail> metricDeets, BufferedReader commandOutput, List<Integer> skipColumns) throws Exception {
		String line, nextLine;
		String[] metricNames = null, metricValues;	
		while((line = commandOutput.readLine()) != null) {
			getLogger().finest("Origin Line: " + line);
			line = line.replaceAll("([A-Za-z-]+): ", "$0 ").replaceAll(" % ", " ").replaceAll("/", "-").trim();
			//line = line.replaceAll("% ", " ").replaceAll("/", "-").trim();
			getLogger().finest("Modded Line: " + line);
			getLogger().finest("complexHeaderLinePattern: " + complexHeaderLinePattern.matcher(line).matches());
			getLogger().finest("dashesPattern: " + dashesPattern.matcher(line).matches());
			getLogger().finest("multiValueLinePattern: " + multiValueLinePattern.matcher(line).matches());
			getLogger().finest("complexValueLinePattern: " + complexValueLinePattern.matcher(line).matches());	
			if((metricNames != null) && complexValueLinePattern.matcher(line).matches()) {	
				getLogger().finest("Complex Value Ahoy!");
				// Assume 1st column is prefix to metric
				metricValues = line.replaceAll("/", "-").replaceAll("[%:]+ ", " ").split("\\s+");
				if (metricValues[0].charAt(0) == '-') {
					metricValues[0] = metricValues[0].substring(1);
				}
				getLogger().finer("Complex - Names (count " + metricNames.length + "): " + Arrays.toString(metricNames));
				getLogger().finer("Complex - Values (count " + metricValues.length + "): " + Arrays.toString(metricValues));	
				
				
				int j = 1;
				int ulimit = metricValues.length;
				if ((metricNames.length + 1) == metricValues.length) {
					j = 0;
				} else if ((metricNames.length + 1) < metricValues.length) {
					getLogger().finer("Number of Values (" + metricValues.length + ") exceeds number of Names (" + metricNames.length + ")");
					ulimit = metricNames.length;
				} else if (metricNames.length > metricValues.length) {
					getLogger().finer("Number of Names (" + metricNames.length + ") exceeds number of Values (" + metricValues.length + ")");
				}

				for (int i=1; i<ulimit; i++) {
					if(skipColumns == null || !skipColumns.contains(i-1)) {
						insertMetric(currentMetrics, metricDeets, mungeString(thisCommand, metricNames[j]), metricValues[0], metricValues[i]);
					}
					j++;
				}
			} else if((metricNames != null) && multiValueLinePattern.matcher(line).matches()) {	
				getLogger().finer("We have a number line!");
				metricValues = line.split("\\s+");
				getLogger().finer("Multi - Names (count " + metricNames.length + "): " + Arrays.toString(metricNames));
				getLogger().finer("Multi - Values (count " + metricValues.length + "): " + Arrays.toString(metricValues));
				int ulimit = metricValues.length;
				if (metricNames.length < metricValues.length) {
					getLogger().finer("Number of Values (" + metricValues.length + ") exceeds number of Names (" + metricNames.length + ")");
					ulimit = metricNames.length;
				} else if (metricNames.length > metricValues.length) {
					getLogger().finer("Number of Names (" + metricNames.length + ") exceeds number of Values (" + metricValues.length + ")");
					
				}
				for (int i=0; i<ulimit; i++) {
					insertMetric(currentMetrics, metricDeets, mungeString(thisCommand, metricNames[i]), "", metricValues[i]);
				}
			} else if (complexHeaderLinePattern.matcher(line).matches() && !dashesPattern.matcher(line).matches()) {
				getLogger().finest("Complex Header Line Ahoy!");
				commandOutput.mark(BUFFER_SIZE);
				if((nextLine = commandOutput.readLine()) != null) {
					nextLine = nextLine.trim();
					getLogger().finer("Checking next line: " + nextLine);
					if(dashesPattern.matcher(nextLine).matches()) {
						getLogger().finer("Line of dashes detected");
						continue;
					} else if (complexValueLinePattern.matcher(nextLine).matches()) {
						getLogger().finer("Next line is value line");
						// Next line is values, so reset to 'mark' point such that this line will be read on the next cycle
						commandOutput.reset();
					} else if(complexHeaderLinePattern.matcher(nextLine).matches()) {
						getLogger().finer("Next line is header line");
						line = nextLine;
					} else {
						getLogger().finer("Next line is value line");
						// Next line is values, so reset to 'mark' point such that this line will be read on the next cycle
						commandOutput.reset();
					}
					metricNames = line.replaceAll("([A-Z]+[a-z]+) ([a-z]+)","$1_$2").replaceAll("[A-Za-z-_]+:\\s+","").split("\\s+");
				}
			}		
		}
		commandOutput.close();
	}
	
	public void parseMultiMetricOutput(String thisCommand, HashMap<String, MetricOutput> currentMetrics, HashMap<String,MetricDetail> metricDeets, BufferedReader commandOutput, List<Integer> skipColumns) throws Exception {
		String line, nextLine;
		String[] metricNames = null, metricValues;
		while((line = commandOutput.readLine()) != null) {
			line = line.replaceAll("([a-z-]+):", "").replaceAll(" % ", " %").trim();
			getLogger().finer("Line: " + line);
			if((metricNames != null) && multiValueLinePattern.matcher(line).matches()) {	
				getLogger().finer("We have a number line!");
				metricValues = line.split("\\s+");
				getLogger().finer("Multi - Names (count " + metricNames.length + "): " + Arrays.toString(metricNames));
				getLogger().finer("Multi - Values (count " + metricValues.length + "): " + Arrays.toString(metricValues));
				int ulimit = metricValues.length;
				if (metricNames.length < metricValues.length) {
					getLogger().finer("Number of Values (" + metricValues.length + ") exceeds number of Names (" + metricNames.length + ")");
					ulimit = metricNames.length;
				} else if (metricNames.length > metricValues.length) {
					getLogger().finer("Number of Names (" + metricNames.length + ") exceeds number of Values (" + metricValues.length + ")");
				}
				for (int i=0; i<ulimit; i++) {
					insertMetric(currentMetrics, metricDeets, mungeString(thisCommand, metricNames[i]), "", metricValues[i]);
				}
				break;
			} else if (multiHeaderLinePattern.matcher(line).matches() && !dashesPattern.matcher(line).matches()) {
				getLogger().finer("We have a header line!");
				commandOutput.mark(BUFFER_SIZE);
				if((nextLine = commandOutput.readLine()) != null) {
					nextLine = nextLine.trim();
					getLogger().finer("Checking next line: " + nextLine);
					if(dashesPattern.matcher(nextLine).matches()) {
						getLogger().finer("Line of dashes detected");
						metricNames = line.replaceAll("[\\w-]+:\\s+", "").split("\\s+");
						continue;
					} else if(multiValueLinePattern.matcher(nextLine).matches()) {
						getLogger().finer("Next line is value line");
						// Next line is values, so reset to 'mark' point such that this line will be read on the next cycle
						commandOutput.reset();
					} else if(multiHeaderLinePattern.matcher(nextLine).matches()) {
						getLogger().finer("Next line is actual header line");
						line = nextLine;
					} else {
						getLogger().finer("Nothing to see here");
						continue;
					}
					metricNames = line.split("\\s+");
				}
			}
		}
		commandOutput.close();
	}
	
	public HashMap<String,Number> parseSingleMetricOutput(String thisCommand, BufferedReader commandOutput) throws Exception {
		HashMap<String,Number> output = new HashMap<String,Number>();
		
		String line;
		
		while((line = commandOutput.readLine()) != null) {
			line = line.trim();
			if (singleMetricLinePattern.matcher(line).matches() && !dashesPattern.matcher(line).matches()) {
				String[] lineSplit = line.split("\\s+");	
				try {
					String metricName = Arrays.toString(Arrays.copyOfRange(lineSplit, 1, lineSplit.length)).replaceAll("[\\[\\],]*", "");
					double metricValue = Double.parseDouble(lineSplit[0]);
					output.put(mungeString(thisCommand, metricName), metricValue);
				} catch(NumberFormatException e) {
					// Means the 1st field is not a number. Value is ignored.
				}
			}
		}
		commandOutput.close();
		return output;
	}
	
	public void insertMetric(HashMap<String, MetricOutput> currentMetrics, HashMap<String,MetricDetail> metricDeets, 
			String metricName, String metricPrefix, String metricValueString) {
		String fullMetricName;
		double metricValue;
		
		try {
		    metricValue = Double.parseDouble(metricValueString);
		} catch(NumberFormatException e) {
			// If not a number, don't insert (return from method)
		    return;
		}
		
		if(!metricPrefix.isEmpty()) {
			fullMetricName = mungeString(metricPrefix, metricName);
		} else {
			fullMetricName = metricName;
		}
		
		if(currentMetrics.containsKey(fullMetricName)) {
			MetricOutput thisMetric = currentMetrics.get(fullMetricName);
			thisMetric.setValue(metricValue);
			currentMetrics.put(fullMetricName, thisMetric);
		} else if (metricDeets.containsKey(metricName)) {
			currentMetrics.put(fullMetricName, new MetricOutput(metricDeets.get(metricName), metricPrefix, metricValue));
		}		
	}
	
	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public String mungeString(String str1, String str2) {
		return str1 + "/" + str2;
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
	
	public void printMetrics(HashMap<String, MetricOutput> outputMetrics) {
		Iterator<String> outputIterator = outputMetrics.keySet().iterator();  
		   
		while (outputIterator.hasNext()) {  
		   String thisKey = outputIterator.next().toString();  
		   MetricOutput thisMetric = outputMetrics.get(thisKey);
		   MetricDetail thisMetricDetail = thisMetric.getMetricDetail();
		   if (thisMetric.getNamePrefix().isEmpty()) {
			   getLogger().finer(mungeString(thisMetricDetail.getPrefix(), thisMetricDetail.getName()) +
					   ", " + thisMetric.getValue() + " " + thisMetricDetail.getUnits());
		   } else {
			   getLogger().finer(mungeString(thisMetricDetail.getPrefix(), 
					   mungeString(thisMetric.getNamePrefix(), thisMetricDetail.getName())) +
					   ", " + thisMetric.getValue() + " " + thisMetricDetail.getUnits());
		   }
		   
		}
	}

	public void printMetricsSimple(HashMap<String, Number> outputMetrics) {
		Iterator<Entry<String, Number>> outputIterator = outputMetrics.entrySet().iterator();  
		while (outputIterator.hasNext()) { 
			Map.Entry<String, Number> pairs = outputIterator.next();
			getLogger().finer(pairs.getKey() + ", " + getMetricType(pairs.getKey()) + ", " + pairs.getValue());   
		}
	}
}
