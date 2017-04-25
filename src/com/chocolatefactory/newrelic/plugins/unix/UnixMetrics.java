package com.chocolatefactory.newrelic.plugins.unix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;

public abstract class UnixMetrics {
	
	public static final String kCategoryMetricName="Component";
	public static final String kDeltaMetricName="delta";
	public static final String kOverviewMetricName="overview";
	public static final String kDefaultMetricType="value";
	public static final int kDefaultLineLimit = 0;
	public static final int kMetricInterval = 60;
	public static final String kExecutionDelay = Integer.toString(kMetricInterval / 2);
	public static final String kExecutionCount = "2";
	public static final char kMetricTreeDivider='/';
	public static final float kGigabytesToBytes=1073741824;
	public static final float kMegabytesToBytes=1048576;

	public static final String kColumnMetricPrefix = "THIS_IS_THE_PREFIX_OF_A_METRIC_NAME";
	public static final String kColumnMetricProcessName = "THIS_IS_A_PROCESS_NAME_TO_BE_COUNTED";
	public static final String kColumnMetricDiskName = "THIS_IS_A_DISK_NAME";
	public static final String kColumnMetricName = "THIS_IS_THE_LAST_PART_OF_A_METRIC_NAME";
	public static final String kColumnMetricValue = "THIS_IS_THE_ACTUAL_METRIC_VALUE";
	public static final String kMemberPlaceholder = "THIS_IS_A_MEMBER_PLACEHOLDER";
	public static final String kColumnIgnore = "THIS_COLUMN_WILL_BE_IGNORED";
	
	// COMPLEXDIM: multiple metrics per line, can have words in value lines
	// MULTIDIM: multiple metrics per line, can only have numbers (or dashes) in line
	// SINGLEDIM: single metric per line (usually "name value")
	public static enum commandTypes{REGEXLISTDIM, REGEXDIM, SIMPLEDIM};
	
	public HashMap<String, MetricDetail> allMetrics;
	public HashMap<String, UnixCommand> allCommands;
	// Use defaultignores in "new UnixCommand(...)" when retrieving all columns of a table,
	// or when retrieving single-dimensional metrics (1 metric per line)
	public List<Integer> defaultignores;
	
	private int pageSize;
	String[] pageSizeCommand = {"pagesize"};
	
	public void setPageSize() {
		int ps = 0;
		for(String line : CommandMetricUtils.executeCommand(pageSizeCommand)) {
			try {
				ps = Integer.parseInt(line.trim());
				break;
			} catch (NumberFormatException e) { 
				ps = 0;
			}
		}
		pageSize = ps;
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	public UnixMetrics() {
		setPageSize();
		allMetrics = new HashMap<String, MetricDetail>();
		allCommands = new HashMap<String, UnixCommand>();
		defaultignores = new ArrayList<Integer>();
	}
	
	public UnixMetrics(String[] psc) {
		pageSizeCommand = psc;
		setPageSize();
		allMetrics = new HashMap<String, MetricDetail>();
		allCommands = new HashMap<String, UnixCommand>();
		defaultignores = new ArrayList<Integer>();
	}

	class UnixCommand {
		// private String[] command;
		private ArrayList<String[]> commands;
		private commandTypes type;
		private List<Integer> skipColumns;
		private HashMap<Pattern, String[]> lineMappings;
		private int lineLimit;
		private boolean checkAllRegex = false;
		
		UnixCommand(ArrayList<String[]> tcs, commandTypes tt, List<Integer> sc, int ll, HashMap<Pattern, String[]> lm) {
			setCommands(tcs);
			setType(tt);
			setSkipColumns(sc);
			setLineLimit(ll);
			setLineMappings(lm);
		}
		
		UnixCommand(ArrayList<String[]> tcs, commandTypes tt, List<Integer> sc, int ll, boolean re, HashMap<Pattern, String[]> lm) {
			this(tcs, tt, sc, ll, lm);
			setCheckAllRegex(re);
		}
		
		UnixCommand(String[] tc, commandTypes tt, List<Integer> sc, int ll, HashMap<Pattern, String[]> lm) {
			setCommand(tc);
			setType(tt);
			setSkipColumns(sc);
			setLineLimit(ll);
			setLineMappings(lm);
		}
		
		UnixCommand(String[] tc, commandTypes tt, List<Integer> sc, int ll, boolean re, HashMap<Pattern, String[]> lm) {
			this(tc, tt, sc, ll, lm);
			setCheckAllRegex(re);
		}
		
		UnixCommand(String[] tc, commandTypes tt, List<Integer> sc) {
			this(tc, tt, sc, kDefaultLineLimit, null);
		}
		
		public String[] getCommand() {
			return commands.get(0);
		}
		public void setCommand(String[] command) {
			commands = new ArrayList<String[]>();
			this.commands.add(command);
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

		public boolean isCheckAllRegex() {
			return checkAllRegex;
		}

		public void setCheckAllRegex(boolean checkAllRegex) {
			this.checkAllRegex = checkAllRegex;
		}

		public ArrayList<String[]> getCommands() {
			return commands;
		}

		public void setCommands(ArrayList<String[]> commands) {
			this.commands = commands;
		}
    }
}
