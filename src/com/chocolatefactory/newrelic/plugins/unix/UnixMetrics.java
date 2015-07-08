package com.chocolatefactory.newrelic.plugins.unix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;

public abstract class UnixMetrics {
	
	public static final String kCategoryMetricName="Component";
	public static final String kDeltaMetricName="delta";
	public static final String kOverviewMetricName="overview";
	public static final String kDefaultMetricType="ms";
	public static final int kDefaultLineLimit = 0;
	public static final int kMetricInterval = 60;
	public static final char kMetricTreeDivider='/';
	public static final float kGigabytesToBytes=1073741824;
	public static final float kMegabytesToBytes=1048576;
	public static final String[] kInterfaceCommand = new String[]{"ifconfig", "-l"};
	public static final String kColumnMetricPrefix = "THIS_IS_PART_OF_METRIC_NAME";
	public static final String kColumnMetricName = "THIS_IS_THE_LAST_PART_OF_METRIC_NAME";
	public static final String kColumnMetricValue = "THIS_IS_THE_METRIC_VALUE";
	public static final String kInterfacePlaceholder = "INTERFACE_PLACEHOLDER";
	
	public HashMap<String, MetricDetail> allMetrics = new HashMap<String, MetricDetail>();
	public HashMap<String, UnixCommand> allCommands = new HashMap<String, UnixCommand>();
	// Use defaultignores in "new UnixCommand(...)" when retrieving all columns of a table,
	// or when retrieving single-dimensional metrics (1 metric per line)
	public List<Integer> defaultignores = new ArrayList<Integer>();
	
	// COMPLEXDIM: multiple metrics per line, can have words in value lines
	// MULTIDIM: multiple metrics per line, can only have numbers (or dashes) in line
	// SINGLEDIM: single metric per line (usually "name value")
	public static enum commandTypes{INTERFACEDIM, REGEXDIM, SIMPLEDIM};
	
	class UnixCommand {
		private String[] command;
		private commandTypes type;
		private List<Integer> skipColumns;
		private HashMap<Pattern, String[]> lineMappings;
		private int lineLimit;
		
		UnixCommand(String[] tc, commandTypes tt, List<Integer> sc, int ll, HashMap<Pattern, String[]> lm) {
			setCommand(tc);
			setType(tt);
			setSkipColumns(sc);
			setLineLimit(ll);
			setLineMappings(lm);
		}
		
		UnixCommand(String[] tc, commandTypes tt, List<Integer> sc) {
			this(tc, tt, sc, kDefaultLineLimit, null);
		}
		
		public String[] getCommand() {
			return command;
		}
		public void setCommand(String[] command) {
			this.command = command;
		}
		public commandTypes getType() {
			return type;
		}
		public void setType(commandTypes type) {
			this.type = type;
		}

		public List<Integer> getSkipColumns() {
			return skipColumns;
		}

		public void setSkipColumns(List<Integer> skipColumns) {
			this.skipColumns = skipColumns;
		}

		public HashMap<Pattern, String[]> getLineMappings() {
			return lineMappings;
		}

		public void setLineMappings(HashMap<Pattern, String[]> lineMappings) {
			if(lineMappings != null)
				this.lineMappings = lineMappings;
		}

		public int getLineLimit() {
			return lineLimit;
		}

		public void setLineLimit(int lineLimit) {
			this.lineLimit = lineLimit;
		}
    }
}
