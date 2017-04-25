package com.chocolatefactory.newrelic.plugins.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.unix.UnixAgent;
import com.chocolatefactory.newrelic.plugins.unix.UnixMetrics;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail.metricTypes;
import com.newrelic.metrics.publish.util.Logger;

public class CommandMetricUtils {

	public static final String kPluginJarName = "newrelic_unix_plugin";
	
	private static Pattern dashesPattern = Pattern.compile("\\s*[\\w-]+(\\s+[-]+)+(\\s[\\w-]*)*");
	private static Pattern singleMetricLinePattern = Pattern.compile("\\S*(\\d+)\\s+([\\w-%\\(\\)])(\\s{0,1}[\\w-%\\(\\)])*");
	private static final Logger logger = Logger.getLogger(UnixAgent.class);
	
	public static void addSummaryMetrics(HashMap<String, MetricOutput> currentMetrics) throws Exception {
		String metricName = null, metricUnits = null, metricPrefix = "Summary";
		double metricValue = 0.0, memFree = 0.0, memUsed = 0.0, memTotal = 0.0;
		boolean memFreeSet = false, memUsedSet = false, memTotalSet = false;
		HashMap<String, MetricOutput> newMetrics = new HashMap<String, MetricOutput>();
		
		for (MetricOutput thisMetric : currentMetrics.values()) {
			String thisMetricName = thisMetric.getMetricDetail().getName();
			String thisMetricPrefix = thisMetric.getMetricDetail().getPrefix();
			double thisMetricValue = thisMetric.getValue().doubleValue();
			if(thisMetricPrefix.equals("CPU") && thisMetricName.equals("Idle")) {
				metricValue = 100 - thisMetricValue;
				metricName = "CPU Utilization";
				metricUnits = "%";
			} else if(thisMetricPrefix.equals("Disk") && thisMetricName.equals("Used") 
					&& !thisMetric.getMetricDetail().getUnits().equals("kb") && thisMetricValue > metricValue) {
				metricValue = thisMetricValue;
				metricName = "Fullest Disk";
				metricUnits = "%";
			} else if(thisMetricPrefix.startsWith("Memory") && !thisMetricPrefix.startsWith("MemoryDetailed") && !thisMetricName.startsWith("Swap") 
					&& thisMetric.getMetricDetail().getUnits().equals("kb")) {
				if (thisMetric.getMetricDetail().getName().endsWith("Free")) {
					memFreeSet = true;
					memFree = thisMetricValue;
				} else if (thisMetric.getMetricDetail().getName().endsWith("Used")) {
					memUsedSet = true;
					memUsed = thisMetricValue;
				} else if (thisMetric.getMetricDetail().getName().endsWith("Total")) {
					memTotalSet = true;
					memTotal = thisMetricValue;
				}
			}
			
			if(metricName != null && metricUnits != null) {
				newMetrics.put(mungeString(metricPrefix, metricName), 
					new MetricOutput(new MetricDetail(metricPrefix, metricName, metricUnits, metricTypes.NORMAL, 1), 
					"", roundNumber(metricValue, 2)));
				metricName = null;
				metricUnits = null;
			}
		}
		
		if((memUsedSet && memFreeSet) || (memUsedSet && memTotalSet) || (memFreeSet && memTotalSet)) {
			metricName = "Memory Utilization";
			metricUnits = "%";
			if(memFreeSet && memTotalSet) {
				metricValue = (1 - (memFree / memTotal)) * 100;
				logger.debug("Mem Free: " + memFree);
				logger.debug("Mem Total: " + memTotal);
			} else if (memUsedSet && memTotalSet) {
				metricValue = (memUsed / memTotal) * 100;
				logger.debug("Mem Used: " + memUsed);
				logger.debug("Mem Total: " + memTotal);
			} else {
				metricValue = (memUsed / (memFree + memUsed)) * 100;
				logger.debug("Mem Free: " + memFree);
				logger.debug("Mem Used: " + memUsed);
			}
			
			logger.debug("Mem Utilization: " + metricValue);
			
			if(metricName != null && metricUnits != null) {
				newMetrics.put(mungeString(metricPrefix, metricName), 
					new MetricOutput(new MetricDetail(metricPrefix, metricName, metricUnits, metricTypes.NORMAL, 1), 
					"", roundNumber(metricValue, 2)));
			}
		}
		
		if (!newMetrics.isEmpty()) {
			currentMetrics.putAll(newMetrics);
		}
	}
	
	public static ArrayList<String> executeCommand(ArrayList<String[]> commands) {
		return executeCommand(commands, false);
	}
	
	public static ArrayList<String> executeCommand(ArrayList<String[]> commands, Boolean useFile) {
		ArrayList<String> al = new ArrayList<String>();
		for(int i=0; i< commands.size(); i++) {
			al.addAll(executeCommand(commands.get(i), useFile));
		}
		return al;
	}
	
	public static ArrayList<String> executeCommand(String[] command) {
		return executeCommand(command, false);
	}
	
	public static ArrayList<String> executeCommand(String[] command, Boolean useFile) {
		Runtime rt = Runtime.getRuntime();
		BufferedReader br = null;
		ArrayList<String> al = new ArrayList<String>();
		String line;
		
		if (useFile) {
			File commandFile = new File(command + ".out");
			CommandMetricUtils.logger.debug("Opening file: "
					+ commandFile.getAbsolutePath());
			if (!commandFile.exists()) {
				CommandMetricUtils.logger.error("Error: "
						+ commandFile.getAbsolutePath() + " does not exist.");
			} else if (!commandFile.isFile()) {
				CommandMetricUtils.logger.error("Error: "
						+ commandFile.getAbsolutePath() + " is not a file.");
			} else {
				try {
					br = new BufferedReader(new FileReader(commandFile));
					while((line = br.readLine()) != null) {
						al.add(line);
					}
				} catch (Exception e) {
					CommandMetricUtils.logger.error("Error: "
							+ commandFile.getAbsolutePath()
							+ " does not exist.");
					e.printStackTrace();
				} finally {
					try {
						br.close();
					} catch (IOException e) {
						// If we can't close, then it's probably closed.
					}
				}
			}
		} else {
			Process proc = null;
			try {
				if (command != null) {
					CommandMetricUtils.logger.debug("Begin execution of "
							+ Arrays.toString(command));
					proc = rt.exec(command);
					br = new BufferedReader(new InputStreamReader(
							proc.getInputStream()));
					while((line = br.readLine()) != null) {
						al.add(line);
						logger.debug(" Line: " + line);
					}
				} else {
					CommandMetricUtils.logger.error("Error: command was null.");
				}
			} catch (Exception e) {
				CommandMetricUtils.logger.error("Error: Execution of "
						+ Arrays.toString(command) + " failed.");
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (Exception e) {
					// If we can't close, then it's probably closed.
				} finally {
					if (proc != null) {
						proc.destroy();
					}
				}
			}
		}
		return al;
	}

	public static String getSimpleMetricType(String metricInput) {
		if (metricInput.contains("percentage")) {
			return "%";
		} else {
			for (String thisKeyword : metricInput.split("\\s")) {
				if (thisKeyword.endsWith("s")) {
					return thisKeyword;
				}
			}
		}
		return "ms";
	}
	
	private static void insertMetric(HashMap<String, MetricOutput> currentMetrics,
		HashMap<String, MetricDetail> metricDeets, String metricName,
		String metricPrefix, String metricValueString) {

		String fullMetricName;
		double metricValue;
		// Set Metric names to lower-case to limit headache of OS version differences
		String metricNameLower = metricName.toLowerCase();

		try {
			metricValue = Double.parseDouble(metricValueString);
		} catch (NumberFormatException e) {
			// If not a number, don't insert (return from method)
			return;
		}

		if (!metricPrefix.isEmpty()) {
			fullMetricName = CommandMetricUtils.mungeString(metricPrefix, metricNameLower);
		} else {
			fullMetricName = metricNameLower;
		}

		if (currentMetrics.containsKey(fullMetricName)) {
			MetricOutput thisMetric = currentMetrics.get(fullMetricName);
			thisMetric.setValue(metricValue);
			thisMetric.setCurrent(true);
			currentMetrics.put(fullMetricName, thisMetric);
		} else if (metricDeets.containsKey(metricNameLower)) {
			currentMetrics.put(fullMetricName, 
				new MetricOutput(metricDeets.get(metricNameLower), metricPrefix, metricValue));
		} else if (metricDeets.containsKey(metricName)) {
			currentMetrics.put(fullMetricName,
				new MetricOutput(metricDeets.get(metricName), metricPrefix,	metricValue));
		} else {
			currentMetrics.put(fullMetricName, new MetricOutput(
				new MetricDetail(metricPrefix, metricName, UnixMetrics.kDefaultMetricType, MetricDetail.metricTypes.NORMAL, 1),
				metricPrefix, metricValue));
		}
	}

	public static String mungeString(String str1, String str2) {
		if (str1.isEmpty()) {
			return str2;
		} else if (str2.isEmpty()) {
			return str1;
		} else {
			return str1 + UnixMetrics.kMetricTreeDivider + str2;
		}
	}
	
	public static void parseRegexMetricOutput(String thisCommand,
		HashMap<Pattern, String[]> lineMappings, String metricPrefix,
		int lineLimit, boolean checkAllRegex, HashMap<String, MetricOutput> currentMetrics,
		HashMap<String, MetricDetail> metricDeets,
		ArrayList<String> commandOutput) throws Exception {
		
		int lineCount = 0;
		lineloop: for(String line : commandOutput) {
			regexloop: for (Map.Entry<Pattern, String[]> lineMapping : lineMappings.entrySet()) {
				Pattern lineRegex = lineMapping.getKey();
				String[] lineColumns = lineMapping.getValue();
				Matcher lineMatch = lineRegex.matcher(line.trim());
				if (lineMatch.matches()) {
					logger.debug("Matched: " + line);
					String thisMetricPrefix = metricPrefix;
					String thisMetricName = "";
					String thisMetricValueString = "";
					
					// Loop through columns of regexed line twice
					// First loop - get metric prefixes
					for (int l = 0; l < lineColumns.length; l++) {
						if(lineColumns[l] == UnixMetrics.kColumnMetricPrefix) {
							String thisPrefix = lineMatch.group(l + 1);
							thisMetricPrefix = CommandMetricUtils.mungeString(thisMetricPrefix, thisPrefix.replaceAll("/", "-"));
						} else if(lineColumns[l] == UnixMetrics.kColumnMetricDiskName) {
							String thisPrefix = lineMatch.group(l + 1);
							thisMetricPrefix = CommandMetricUtils.mungeString(
								thisMetricPrefix, thisPrefix.substring(thisPrefix.lastIndexOf('/') + 1));
						} else if(lineColumns[l] == UnixMetrics.kColumnMetricProcessName) {
							String thisPrefix = lineMatch.group(l + 1);
							if(thisPrefix.contains(UnixAgent.kAgentGuid) || thisPrefix.contains(kPluginJarName)) {
								thisMetricPrefix = CommandMetricUtils.mungeString(
									thisMetricPrefix, kPluginJarName);
							} else {
								String processCommand = thisPrefix.split("\\s+")[0];
								if (processCommand.startsWith("[") && processCommand.endsWith("]")) {
									thisMetricPrefix = CommandMetricUtils.mungeString(
										thisMetricPrefix, processCommand.replace("]","").replace("[", "").split("/")[0]);
								} else {
									thisMetricPrefix = CommandMetricUtils.mungeString(
										thisMetricPrefix, processCommand.substring(processCommand.lastIndexOf('/') + 1));
								}
							}
						}
					}
					
					// Second loop - get metrics
					for (int m = 0; m < lineColumns.length; m++) {
						if (lineColumns[m] == UnixMetrics.kColumnMetricPrefix ||
								lineColumns[m] == UnixMetrics.kColumnMetricDiskName ||
								lineColumns[m] == UnixMetrics.kColumnIgnore) {
							continue;
						} else if (lineColumns[m] == UnixMetrics.kColumnMetricName) {
							thisMetricName = lineMatch.group(m + 1).replaceAll("/", "-");
						} else if (lineColumns[m] == UnixMetrics.kColumnMetricValue) {
							thisMetricValueString = lineMatch.group(m + 1);
						} else if (lineColumns[m] == UnixMetrics.kColumnMetricProcessName) {
							CommandMetricUtils.insertMetric(currentMetrics,
							metricDeets, CommandMetricUtils.mungeString(thisCommand, lineColumns[m]),
							thisMetricPrefix, "1");
						} else {
							CommandMetricUtils.insertMetric(currentMetrics,
								metricDeets, CommandMetricUtils.mungeString(thisCommand, lineColumns[m]),
								thisMetricPrefix, lineMatch.group(m + 1));
						}
					}
					
					// If kColumnMetricName & kColumnMetricValue were used to get metric name & value, 
					// finally report this metric
					if(!thisMetricName.isEmpty() && !thisMetricValueString.isEmpty()) {
						CommandMetricUtils.insertMetric(currentMetrics,
								metricDeets, CommandMetricUtils.mungeString(thisCommand, thisMetricName),
								thisMetricPrefix, thisMetricValueString);
					}
						
					// Once we find a valid mapping for this line, 
					// stop looking for matches for this line,
					// unless we explicitly want to check all regex mappings.
					if(!checkAllRegex) {
						break regexloop;
					}
				} else {
					logger.debug("Skipped: " + line);
				}
			}

			// For commands like 'top', we probably only need the first few lines.
			if (lineLimit > 0) {
				lineCount++;
				if (lineCount >= lineLimit) {
					break lineloop;
				}
			}
		}
	}

	public static HashMap<String, Number> parseSimpleMetricOutput(
		String thisCommand, ArrayList<String> commandOutput) throws Exception {
		
		HashMap<String, Number> output = new HashMap<String, Number>();
		for(String line : commandOutput) {
			line = line.trim();
			if (CommandMetricUtils.singleMetricLinePattern.matcher(line).matches()
					&& !CommandMetricUtils.dashesPattern.matcher(line).matches()) {
				String[] lineSplit = line.split("\\s+");
				try {
					String metricName = Arrays.toString(Arrays.copyOfRange(lineSplit, 1, lineSplit.length))
							.replaceAll("[\\[\\],]*", "");
					double metricValue = Double.parseDouble(lineSplit[0]);
					output.put(CommandMetricUtils.mungeString(thisCommand, metricName), metricValue);
				} catch (NumberFormatException e) {
					// Means the 1st field is not a number. Value is ignored.
				}
			}
		}
		return output;
	}
	
	// Resets current metrics to "false" and removes stale metrics
	public static HashMap<String, MetricOutput> resetCurrentMetrics(
			HashMap<String,MetricOutput> inputMetrics) {
		HashMap<String, MetricOutput> outputMetrics = new HashMap<String, MetricOutput>();
		for(String thisKey : inputMetrics.keySet()) {
			MetricOutput thisMetric = inputMetrics.get(thisKey);
			// If it's current, set to "false" for next iteration and transpose to output.
			// Reset incrementors to 'O' for next go-round
			if (thisMetric.isCurrent()) {
				thisMetric.setCurrent(false);
				if(thisMetric.getMetricDetail().getType().equals(MetricDetail.metricTypes.INCREMENT)) {
					thisMetric.resetValue();
				}
				outputMetrics.put(thisKey, thisMetric);
			}
		}
		return outputMetrics;
	}
	
	public static String[] replaceInArray(String[] thisArray, String findThis, String replaceWithThis) {
		String[] outputArray = new String[thisArray.length];
		for (int i=0; i < thisArray.length; i++) {
			outputArray[i] = thisArray[i].replaceAll(findThis, replaceWithThis);
		}
		return outputArray;
	}
	
	public static ArrayList<String[]> replaceInArray(ArrayList<String[]> thisArrayList, String findThis, String replaceWithThis) {
		ArrayList<String[]> outputArrayList = new ArrayList<String[]>();
		for (int i=0; i < thisArrayList.size(); i++) {
			outputArrayList.add(replaceInArray(thisArrayList.get(i), findThis, replaceWithThis));
		}
		return outputArrayList;
	}
	
	public static double roundNumber(double theNumber, int places) {
		double placesDouble = Math.pow(10, places);
		return Math.round(theNumber * placesDouble) / placesDouble;
	}
}
