package com.chocolatefactory.newrelic.plugins.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.HashMap;

import com.newrelic.metrics.publish.binding.Context;

public class CommandMetricUtils {

	private Runtime rt;
	private Logger logger;
	private Pattern headerPattern = Pattern.compile("[\\w-%:]+(\\s+[\\w-%:]+)*");
	//private Pattern headerPattern = Pattern.compile("\\S+(\\s+\\S+)+");
	private Pattern headerDashesPattern = Pattern.compile("[-]+(\\s+[-]+)+");
	private Pattern singleLineValuePattern = Pattern.compile("\\d+\\.*\\d*%?(\\s+\\d+\\.*\\d*%?)*");
	private Pattern multiLineValuePattern = Pattern.compile("\\S+(\\s+\\S+)+");
	private Pattern lineHasNumbersPattern = Pattern.compile(".*\\d.*");
	private Pattern lineHasWordsAndDashesPattern = Pattern.compile(".*[-]+.*\\w+.*");
	private Pattern singleLineMetricsPattern = Pattern.compile("\\S*(\\d+)\\s+([\\w-%\\(\\)])(\\s{0,1}[\\w-%\\(\\)])*");
	
	public CommandMetricUtils() {
		setLogger(Context.getLogger());
		rt = Runtime.getRuntime();		
	}
		
	public BufferedReader executeCommand(String[] command, Boolean useFile) {
		BufferedReader br;
		if(useFile) {
			File commandFile = new File(command + ".out");
			getLogger().finer("Opening file: " + commandFile.getAbsolutePath());
			if (!commandFile.exists()) {
				getLogger().severe(commandFile.getAbsolutePath() + " does not exist.");
				br = null;
			} else if(!commandFile.isFile()) {
				br = null;
				getLogger().severe(commandFile.getAbsolutePath() + " is not a file.");
			} else {
				try {
					br = new BufferedReader(new FileReader(commandFile));
				} catch (FileNotFoundException e) {
					getLogger().severe(commandFile.getAbsolutePath() + " does not exist.");
					e.printStackTrace();
					br = null;
				}
			}
		} else {
			Process proc;
			try {
				if (command != null) {
					// System.out.println("Running: " + Arrays.toString(command));
					proc = rt.exec(command);
				} else {
					return null;
				}
				br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
				getLogger().severe(Arrays.toString(command) + " failed.");
				br = null;
			}
		}
		
		return br;
	}
	
	public void parseMultiMetricOutput(String thisCommand, HashMap<String, MetricOutput> currentMetrics, HashMap<String,MetricDetail> metricDeets, BufferedReader commandOutput) throws IOException {
		String line, nextLine;
		String[] metricNames, metricValues;
		
		while((line = commandOutput.readLine()) != null) {
			line = line.trim();
			if (headerPattern.matcher(line).matches() && !headerDashesPattern.matcher(line).matches()) {
				metricNames = line.replace("% ", "%").replaceAll("([A-Z]+)\\s{0,1}([A-Z]*[a-z]+):","$1$2").replaceAll("[a-z-]+:", "").split("\\s+");
				
				// Debug
				// System.out.println("Names: " + Arrays.toString(metricNames));
				
				while ((nextLine = commandOutput.readLine()) != null) {
					nextLine = nextLine.trim();
					if (nextLine.isEmpty()) {
						break;
					}
					
					// Debug
					// System.out.println("Next Line: " + nextLine);
					
					if (singleLineValuePattern.matcher(nextLine).matches()) {
						metricValues = nextLine.split("\\s+");
						// Debug
						// System.out.println("Values: " + Arrays.toString(metricValues));
						for (int i=0;i<metricValues.length;i++) {
							insertMetric(currentMetrics, metricDeets, mungeString(thisCommand, metricNames[i]),
									"", metricValues[i]);
						}
						break;
					} else if (multiLineValuePattern.matcher(nextLine).matches() && 
							lineHasNumbersPattern.matcher(nextLine).matches()) {
						// Assume 1st column is prefix to metric
						metricValues = nextLine.replace('%', ' ').replace('/', '_').split("\\s+");
						if (metricValues[0].charAt(0) == '_') {
							metricValues[0] = metricValues[0].substring(1);
						}
						// Debug
						// System.out.println("Multi-Dim Values: " + Arrays.toString(metricValues));
						int j, k;
						for (j=1;j<metricValues.length;j++) {
							if (metricValues.length > metricNames.length) {
								k = j-1;
							} else {
								k = j;
							}
							insertMetric(currentMetrics, metricDeets, mungeString(thisCommand, metricNames[k]), 
									metricValues[0], metricValues[j]);
						}
					} else if (headerPattern.matcher(nextLine).matches()) {
						metricNames = nextLine.replace("% ", "%").replaceAll("([A-Z]+)\\s{0,1}([A-Z]*[a-z]+):","$1$2").replaceAll("[a-z-]+:", "").split("\\s+");
						// Debug
						// System.out.println("Names: " + Arrays.toString(metricNames));
						continue;
					} else if (!lineHasWordsAndDashesPattern.matcher(nextLine).matches()) {
						break;
					}
				}
			}
		}
		commandOutput.close();
	}
	
	public HashMap<String,Number> parseOnePerLineMetricOutput(String thisCommand, BufferedReader commandOutput) throws IOException {
		HashMap<String,Number> output = new HashMap<String,Number>();
		
		String line;
		
		while((line = commandOutput.readLine()) != null) {
			line = line.trim();
			if (singleLineMetricsPattern.matcher(line).matches() && !headerDashesPattern.matcher(line).matches()) {
				String[] lineSplit = line.split("\\s+");	
				try {
					String metricName = Arrays.toString(Arrays.copyOfRange(lineSplit, 1, lineSplit.length)).replaceAll("[\\[\\],]*", "");
					double metricValue = Double.parseDouble(lineSplit[0]);
					output.put(mungeString(thisCommand, metricName), metricValue);
				} catch(NumberFormatException e) {
					// Means the 1st field is not a number. Ignored.
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
}
