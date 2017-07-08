package com.chocolatefactory.newrelic.plugins.newnix.utils;

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

import org.json.simple.JSONArray;

import com.chocolatefactory.newrelic.plugins.newnix.NewNixAgent;
import com.newrelic.metrics.publish.util.Logger;

public class NewCommandMetricUtils {

	public static final String kPluginJarName = "newrelic_unix_plugin";

	private static Pattern dashesPattern = Pattern.compile("\\s*[\\w-]+(\\s+[-]+)+(\\s[\\w-]*)*");
	private static Pattern singleMetricLinePattern = Pattern.compile("\\S*(\\d+)\\s+([\\w-%\\(\\)])(\\s{0,1}[\\w-%\\(\\)])*");
	private static final Logger logger = Logger.getLogger(com.chocolatefactory.newrelic.plugins.newnix.NewNixAgent.class);

	public static void addSummaryMetrics(HashMap<String, NewMetricOutput> currentMetrics) throws Exception {
		HashMap<String, Object> summaryMetric = new HashMap<String, Object>();
		String summaryCategory = "Summary";
		summaryMetric.put("category", summaryCategory);
		summaryMetric.put("ratio", NewNixConstants.kDefaultMetricRatio);
		summaryMetric.put("type", NewNixConstants.kDefaultMetricType);
		double metricValue = 0.0, memFree = 0.0, memUsed = 0.0, memTotal = 0.0;
		boolean memFreeSet = false, memUsedSet = false, memTotalSet = false;
		HashMap<String, NewMetricOutput> newMetrics = new HashMap<String, NewMetricOutput>();

		for (NewMetricOutput thisMetric : currentMetrics.values()) {
			String thisMetricName = (String) thisMetric.getName();
			String thisMetricCategory = (String) thisMetric.getCategory();
			String thisMetricUnits = (String) thisMetric.getUnits();
			double thisMetricValue = thisMetric.getValue().doubleValue();
			if(thisMetricCategory.equals("CPU") && thisMetricName.equals("Idle")) {
				metricValue = 100 - thisMetricValue;
				summaryMetric.put("name", "CPU Utilization");
				summaryMetric.put("units", "%");
			} else if(thisMetricCategory.equals("Disk") && thisMetricName.equals("Used")
					&& !thisMetricUnits.equals("kb") && thisMetricValue > metricValue) {
				metricValue = thisMetricValue;
				summaryMetric.put("name", "Fullest Disk");
				summaryMetric.put("units", "%");
			} else if(thisMetricCategory.startsWith("Memory") && !thisMetricCategory.startsWith("MemoryDetailed") && !thisMetricName.startsWith("Swap")
					&& thisMetricUnits.equals("kb")) {
				if (thisMetricName.endsWith("Free")) {
					memFreeSet = true;
					memFree = thisMetricValue;
				} else if (thisMetricName.endsWith("Used")) {
					memUsedSet = true;
					memUsed = thisMetricValue;
				} else if (thisMetricName.endsWith("Total")) {
					memTotalSet = true;
					memTotal = thisMetricValue;
				}
			}

			if(summaryMetric.containsKey("name") && summaryMetric.containsKey("units")) {
				newMetrics.put(mungeString((String)summaryMetric.get("category"), (String)summaryMetric.get("name")),
					new NewMetricOutput(summaryMetric, roundNumber(metricValue, 2)));
				summaryMetric.remove("name");
				summaryMetric.remove("units");
			}
		}

		if((memUsedSet && memFreeSet) || (memUsedSet && memTotalSet) || (memFreeSet && memTotalSet)) {
			summaryMetric.put("name", "Memory Utilization");
			summaryMetric.put("units", "%");
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

			if(summaryMetric.get("name") != null && summaryMetric.get("units") != null) {
				newMetrics.put(mungeString(summaryCategory, (String)summaryMetric.get("name")),
					new NewMetricOutput(summaryMetric, roundNumber(metricValue, 2)));
			}
		}

		if (!newMetrics.isEmpty()) {
			currentMetrics.putAll(newMetrics);
		}
	}

	public static ArrayList<String> executeCommand(ArrayList<String> commands) {
		return executeCommand(commands, false);
	}

	public static ArrayList<String> executeCommand(ArrayList<String> commands, Boolean useFile) {
		ArrayList<String> al = new ArrayList<String>();
		for(int i=0; i< commands.size(); i++) {
			al.addAll(executeCommand(commands.get(i), useFile));
		}
		return al;
	}

	public static ArrayList<String> executeCommand(String command) {
		return executeCommand(command, false);
	}

	public static ArrayList<String> executeCommand(String command, Boolean useFile) {
		BufferedReader br = null;
		ArrayList<String> al = new ArrayList<String>();
		String line = null;

		if (useFile) {
			File commandFile = new File(command + ".out");
			NewCommandMetricUtils.logger.debug("Opening file: "
					+ commandFile.getAbsolutePath());
			if (!commandFile.exists()) {
				NewCommandMetricUtils.logger.error("Error: "
						+ commandFile.getAbsolutePath() + " does not exist.");
			} else if (!commandFile.isFile()) {
				NewCommandMetricUtils.logger.error("Error: "
						+ commandFile.getAbsolutePath() + " is not a file.");
			} else {
				try {
					br = new BufferedReader(new FileReader(commandFile));
					while((line = br.readLine()) != null) {
						al.add(line);
					}
				} catch (Exception e) {
					NewCommandMetricUtils.logger.error("Error: "
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
					NewCommandMetricUtils.logger.debug("Begin execution of " + command);
					ProcessBuilder pb = new ProcessBuilder(command.split(" "))
							.redirectErrorStream(true);
					proc = pb.start();
					br = new BufferedReader(new InputStreamReader(
							proc.getInputStream()));
					while((line = br.readLine()) != null) {
						al.add(line);
						logger.debug(" Line: " + line);
					}
					proc.waitFor();
					if(br != null) {
						br.close();
					}
					if(proc != null) {
						proc.getOutputStream().close();
						proc.getErrorStream().close();
						proc.getInputStream().close();
					}
				} else {
					NewCommandMetricUtils.logger.error("Error: command was null.");
				}
			} catch (Exception e) {
				NewCommandMetricUtils.logger.error("Error: Execution of " + command + " failed.");
				e.printStackTrace();
			} finally {
				try {
					if(br != null) {
						br.close();
					}
					if(proc != null) {
						proc.getOutputStream().close();
						proc.getErrorStream().close();
						proc.getInputStream().close();
					}
				} catch (Exception e) {
					// If we can't close, then it's probably closed.
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
	
	private synchronized static void insertMetric(HashMap<String, NewMetricOutput> currentMetrics,
		HashMap<String, Object> metricDeets, String metricPrefix, String metricValueString) {

		String fullMetricName = (String) metricDeets.get("category");
		if (!metricPrefix.isEmpty()) {
			fullMetricName = mungeString(fullMetricName, metricPrefix);
		}
		fullMetricName = mungeString(fullMetricName, (String) metricDeets.get("name"));

		double metricValue;
		try {
			metricValue = Double.parseDouble(metricValueString);
		} catch (NumberFormatException e) {
			// If not a number, don't insert (return from method)
			return;
		}

		if (currentMetrics.containsKey(fullMetricName)) {
			currentMetrics.get(fullMetricName).setValue(metricValue);
			currentMetrics.get(fullMetricName).setCurrent(true);
		} else {
			currentMetrics.put(fullMetricName,
				new NewMetricOutput(metricDeets, metricValue));
		}
	}

	public static String mungeString(String str1, String str2) {
		if (str1.isEmpty()) {
			return str2;
		} else if (str2.isEmpty()) {
			return str1;
		} else {
			return str1 + NewNixConstants.kMetricTreeDivider + str2;
		}
	}

	@SuppressWarnings("unchecked")
	public static void parseRegexMetricOutput(String thisCommand,
		HashMap<String, JSONArray> lineMappings, String metricPrefix,
		HashMap<String, Object> allMetrics,
		int lineLimit, boolean checkAllRegex,
		HashMap<String, NewMetricOutput> currentMetrics, ArrayList<String> commandOutput) throws Exception {

		int lineCount = 0;
		lineloop: for(String line : commandOutput) {
			regexloop: for (Map.Entry<String, JSONArray> lineMapping : lineMappings.entrySet()) {
				Pattern lineRegex = Pattern.compile(lineMapping.getKey());
				JSONArray lineColumns = lineMapping.getValue();
				Matcher lineMatch = lineRegex.matcher(line.trim());
				if (lineMatch.matches()) {
					logger.debug("Matched: " + line);
					String thisMetricPrefix = metricPrefix;
					String thisMetricName = "";
					String thisMetricValueString = "";

					// Loop through columns of regexed line twice
					
					// First loop - get metric prefixes
					for (int l = 0; l < lineColumns.size(); l++) {
						if(lineColumns.get(l).equals(NewNixConstants.kColumnMetricPrefix)) {
							String thisPrefix = lineMatch.group(l + 1);
							thisMetricPrefix = mungeString(thisMetricPrefix, thisPrefix.replaceAll("/", "-"));
						} else if(lineColumns.get(l).equals(NewNixConstants.kColumnMetricDiskName)) {
							String thisPrefix = lineMatch.group(l + 1);
							thisMetricPrefix = mungeString(
								thisMetricPrefix, thisPrefix.substring(thisPrefix.lastIndexOf('/') + 1));
						} else if(lineColumns.get(l).equals(NewNixConstants.kColumnMetricProcessName)) {
							String thisPrefix = lineMatch.group(l + 1);
							if(thisPrefix.contains(NewNixAgent.kAgentGuid) || thisPrefix.contains(kPluginJarName)) {
								thisMetricPrefix = mungeString(
									thisMetricPrefix, kPluginJarName);
							} else {
								String processCommand = thisPrefix.split("\\s+")[0];
								if (processCommand.startsWith("[") && processCommand.endsWith("]")) {
									thisMetricPrefix = mungeString(
										thisMetricPrefix, processCommand.replace("]","").replace("[", "").split("/")[0]);
								} else {
									thisMetricPrefix = mungeString(
										thisMetricPrefix, processCommand.substring(processCommand.lastIndexOf('/') + 1));
								}
							}
						}
					}

					// Second loop - get metrics
					for (int m = 0; m < lineColumns.size(); m++) {
						if (((String)lineColumns.get(m)).equals(NewNixConstants.kColumnMetricPrefix) ||
								((String)lineColumns.get(m)).equals(NewNixConstants.kColumnMetricDiskName) ||
								((String)lineColumns.get(m)).equals(NewNixConstants.kColumnIgnore)) {
							continue;
						} else if (((String)lineColumns.get(m)).equals(NewNixConstants.kColumnMetricName)) {
							thisMetricName = lineMatch.group(m + 1).replaceAll("/", "-");
						} else if (((String)lineColumns.get(m)).equals(NewNixConstants.kColumnMetricValue)) {
							thisMetricValueString = lineMatch.group(m + 1);
						} else if (allMetrics.containsKey(lineColumns.get(m))) {
							HashMap<String, Object> thisMetric = (HashMap<String, Object>)allMetrics.get(lineColumns.get(m));
							if(((String)lineColumns.get(m)).equals(NewNixConstants.kColumnMetricProcessName)) {
								NewCommandMetricUtils.insertMetric(currentMetrics,
										thisMetric,
										thisMetricPrefix, "1");
							} else {
								NewCommandMetricUtils.insertMetric(currentMetrics,
										thisMetric,
										thisMetricPrefix, lineMatch.group(m + 1));
							}
						} else {
							logger.debug("There is no matching metric in the JSON for: " + lineColumns.get(m));
						}
					}

					// If kColumnMetricName & kColumnMetricValue were used to get metric name & value,
					// finally report this metric
					if(!thisMetricName.isEmpty() && !thisMetricValueString.isEmpty()) {
						if(allMetrics != null && allMetrics.get(thisMetricName) != null) {
							NewCommandMetricUtils.insertMetric(currentMetrics,
								(HashMap<String, Object>)allMetrics.get(thisMetricName),
								thisMetricPrefix, thisMetricValueString);
						} else {
					        HashMap<String, Object> thisMetricMap = new HashMap<String, Object>();
					        thisMetricMap.put("name", thisMetricName);
					        thisMetricMap.put("category", thisCommand);
					        thisMetricMap.put("ratio", NewNixConstants.kDefaultMetricRatio);
					        thisMetricMap.put("type", NewNixConstants.kDefaultMetricType);
					        thisMetricMap.put("units", NewNixConstants.kDefaultMetricUnits);
							NewCommandMetricUtils.insertMetric(currentMetrics, thisMetricMap,
									thisMetricPrefix, thisMetricValueString);
						}
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
			if (NewCommandMetricUtils.singleMetricLinePattern.matcher(line).matches()
					&& !NewCommandMetricUtils.dashesPattern.matcher(line).matches()) {
				String[] lineSplit = line.split("\\s+");
				try {
					String metricName = Arrays.toString(Arrays.copyOfRange(lineSplit, 1, lineSplit.length))
							.replaceAll("[\\[\\],]*", "");
					double metricValue = Double.parseDouble(lineSplit[0]);
					output.put(mungeString(thisCommand, metricName), metricValue);
				} catch (NumberFormatException e) {
					// Means the 1st field is not a number. Value is ignored.
				}
			}
		}
		return output;
	}

	// Resets current metrics to "false" and removes stale metrics
	public static HashMap<String, NewMetricOutput> resetCurrentMetrics(
			HashMap<String, NewMetricOutput> metricOutput) {
		HashMap<String, NewMetricOutput> outputMetrics = new HashMap<String, NewMetricOutput>();
		for(String thisKey : metricOutput.keySet()) {
			NewMetricOutput thisMetric = metricOutput.get(thisKey);
			// If it's current, set to "false" for next iteration and transpose to output.
			// Reset incrementors to 'O' for next go-round
			if (thisMetric.isCurrent()) {
				thisMetric.setCurrent(false);
				if(thisMetric.getType().equals("INCREMENT")) {
					thisMetric.resetValue();
				}
				outputMetrics.put(thisKey, thisMetric);
			}
		}
		return outputMetrics;
	}

	public static double roundNumber(double theNumber, int places) {
		double placesDouble = Math.pow(10, places);
		return Math.round(theNumber * placesDouble) / placesDouble;
	}
}
