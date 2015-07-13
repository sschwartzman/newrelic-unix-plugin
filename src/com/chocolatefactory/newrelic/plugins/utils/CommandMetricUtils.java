package com.chocolatefactory.newrelic.plugins.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.unix.UnixAgent;
import com.chocolatefactory.newrelic.plugins.unix.UnixMetrics;
import com.newrelic.metrics.publish.util.Logger;

public class CommandMetricUtils {

	private static Runtime rt = Runtime.getRuntime();
	private static Pattern dashesPattern = Pattern.compile("\\s*[\\w-]+(\\s+[-]+)+(\\s[\\w-]*)*");
	private static Pattern singleMetricLinePattern = Pattern.compile("\\S*(\\d+)\\s+([\\w-%\\(\\)])(\\s{0,1}[\\w-%\\(\\)])*");
	private static final Logger logger = Logger.getLogger(UnixAgent.class);
	
	public static BufferedReader executeCommand(String[] interfaceCommand) {
		return executeCommand(interfaceCommand, false);
	}
	
	public static BufferedReader executeCommand(String[] command, Boolean useFile) {
		
		BufferedReader br = null;
		
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
				} catch (Exception e) {
					CommandMetricUtils.logger.error("Error: "
							+ commandFile.getAbsolutePath()
							+ " does not exist.");
					e.printStackTrace();
					br = null;
				}
			}
		} else {
			Process proc = null;
			try {
				if (command != null) {
					CommandMetricUtils.logger.debug("Begin execution of "
							+ Arrays.toString(command));
					proc = CommandMetricUtils.rt.exec(command);
					br = new BufferedReader(new InputStreamReader(
							proc.getInputStream()));
				} else {
					CommandMetricUtils.logger.error("Error: command was null.");
				}
			} catch (Exception e) {
				CommandMetricUtils.logger.error("Error: Execution of "
						+ Arrays.toString(command) + " failed.");
				e.printStackTrace();
				br = null;
			} finally {
				if (proc != null) {
					try {
						proc.waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return br;
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
		BufferedReader commandOutput) throws Exception {
		
		String line;
		int lineCount = 0;
		lineloop: while ((line = commandOutput.readLine()) != null) {
			regexloop: for (Map.Entry<Pattern, String[]> lineMapping : lineMappings.entrySet()) {
				Pattern lineRegex = lineMapping.getKey();
				String[] lineColumns = lineMapping.getValue();
				Matcher lineMatch = lineRegex.matcher(line.trim());
				if (lineMatch.matches()) {
					String thisMetricPrefix = metricPrefix;
					String thisMetricName = "metric"; //default if somehow the metric name isn't set.
					// Loop through twice - first to get metric prefixes
					for (int l = 0; l < lineColumns.length; l++) {
						if (lineColumns[l] == UnixMetrics.kColumnMetricPrefix ||
								lineColumns[l] == UnixMetrics.kColumnMetricPrefixCount) {
							thisMetricPrefix = CommandMetricUtils.mungeString(
								thisMetricPrefix, lineMatch.group(l + 1).replaceAll("/", "-"));
						} 
					}
					// Second loop - get metrics
					for (int m = 0; m < lineColumns.length; m++) {
						if (lineColumns[m] == UnixMetrics.kColumnMetricPrefix) {
							continue;
						} else if (lineColumns[m] == UnixMetrics.kColumnMetricName) {
							thisMetricName = lineMatch.group(m + 1).replaceAll("/", "-");
						} else if (lineColumns[m] == UnixMetrics.kColumnMetricValue) {
							CommandMetricUtils.insertMetric(currentMetrics,
							metricDeets, CommandMetricUtils.mungeString(thisCommand, thisMetricName),
							thisMetricPrefix, lineMatch.group(m + 1));
						} else if (lineColumns[m] == UnixMetrics.kColumnMetricPrefixCount) {
							CommandMetricUtils.insertMetric(currentMetrics,
							metricDeets, CommandMetricUtils.mungeString(thisCommand, lineColumns[m]),
							thisMetricPrefix, "1");
						} else {
							CommandMetricUtils.insertMetric(currentMetrics,
							metricDeets, CommandMetricUtils.mungeString(thisCommand, lineColumns[m]),
							thisMetricPrefix, lineMatch.group(m + 1));
						}
					}
					// Once we find a valid mapping for this line, 
					// stop looking for matches for this line,
					// unless we explicitly want to check all regex mappings.
					if(!checkAllRegex) {
						break regexloop;
					}
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
		String thisCommand, BufferedReader commandOutput) throws Exception {
		
		HashMap<String, Number> output = new HashMap<String, Number>();
		String line;

		while ((line = commandOutput.readLine()) != null) {
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
				if(thisMetric.getMetricDetail().getType() == MetricDetail.metricTypes.INCREMENT) {
					thisMetric.setValue(0);
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
}
