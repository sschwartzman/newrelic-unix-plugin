package com.chocolatefactory.newrelic.plugins.unix;

import java.util.HashMap;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail.metricTypes;

public class OSXMetrics extends UnixMetrics {
	
	public static final String kDefaultAgentName = "MacOSX";
	
	public OSXMetrics() {
	
		super();
		
		/*
		 * Parser & declaration for 'df' command
		 */
		HashMap<Pattern, String[]> dfMapping = new HashMap<Pattern, String[]>();
		dfMapping.put(Pattern.compile("\\s*(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)%.*"),
			new String[]{kColumnMetricDiskName, "1K-blocks", "Used", "Available", "Use%"});
		allCommands.put("df", new UnixCommand(new String[]{"df","-k"}, commandTypes.REGEXDIM, defaultignores, 0, dfMapping));
		allMetrics.put(CommandMetricUtils.mungeString("df", "1K-blocks"), new MetricDetail("Disk", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Used"), new MetricDetail("Disk", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Available"), new MetricDetail("Disk", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Use%"), new MetricDetail("Disk", "Used", "percent", metricTypes.NORMAL, 1));
						
		/*
		 * Parsers & declaration for 'iostat' command
		 */
		HashMap<Pattern, String[]> iostatMapping = new HashMap<Pattern, String[]>();
		iostatMapping.put(Pattern.compile("\\s*([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+).*"),
				new String[]{"KB-t", "xfrs", "MB"});
		allCommands.put("iostat", new UnixCommand(new String[]{"iostat", "-I" , "-d", kMemberPlaceholder}, commandTypes.REGEXLISTDIM, defaultignores, 0, iostatMapping));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "xfrs"), new MetricDetail("DiskIO", "Transfers Per Interval", "transfers", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "MB"), new MetricDetail("DiskIO", "Data Transferred Per Interval", "kb", metricTypes.DELTA, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "KB-t"), new MetricDetail("DiskIO", "Average Request Size", "kb", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for 'netstat' command
		 */
		HashMap<Pattern, String[]> netstatMapping = new HashMap<Pattern, String[]>();
		netstatMapping.put(Pattern.compile("(\\w+\\d*)\\s+\\d+\\s+\\S+\\s+\\S+\\s+"
				+ "(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"),
			new String[]{kColumnMetricPrefix, "Ipkts", "Ierrs", "Ibytes", "Opkts", "Oerrs", "Obytes", "Coll"});	
		allCommands.put("netstat", new UnixCommand(new String[]{"netstat", "-i", "-b"}, commandTypes.REGEXDIM, defaultignores, 0, netstatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Ipkts"), new MetricDetail("Network", "Receive/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Ierrs"), new MetricDetail("Network", "Receive/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Ibytes"), new MetricDetail("Network", "Receive/Bytes", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Opkts"), new MetricDetail("Network", "Transmit/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Oerrs"), new MetricDetail("Network", "Transmit/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Obytes"), new MetricDetail("Network", "Transmit/Bytes", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Coll"), new MetricDetail("Network", "Collisions", "packets", metricTypes.DELTA, 1));
		
		/*
		 * Parser & declaration for 'ps' command
		 */
		HashMap<Pattern, String[]> psMapping = new HashMap<Pattern, String[]>();
		psMapping.put(Pattern.compile("([0-9\\.]+)\\s+([0-9\\.]+)\\s+(\\d+)\\s+(.+)"),
				new String[]{"%CPU", "%MEM", "RSS", kColumnMetricProcessName});
		allCommands.put("ps", new UnixCommand(new String[]{"ps", "-ewwo", "%cpu,%mem,rss,args"}, 
			commandTypes.REGEXDIM, defaultignores, 0, psMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("ps", kColumnMetricProcessName), new MetricDetail("Processes", "Instance Count", "processes", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "%CPU"), new MetricDetail("Processes", "Aggregate CPU", "percent", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "%MEM"), new MetricDetail("Processes", "Aggregate Memory", "percent", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "RSS"), new MetricDetail("Processes", "Aggregate Resident Size", "kb", metricTypes.INCREMENT, 1));
			
		/*
		 * Parsers & declaration for 'top' command
		 */		
		HashMap<Pattern, String[]> topMapping = new HashMap<Pattern, String[]>();
		topMapping.put(Pattern.compile("Load Avg:\\s+([0-9\\.]+),\\s+([0-9\\.]+),\\s+([0-9\\.]+)"), 
			new String[]{"la1", "la5", "la15"});
		topMapping.put(Pattern.compile("Processes:\\s+(\\d+)\\s+total,\\s+(\\d+)\\s+running,\\s+(\\d+)\\s+stuck,\\s+(\\d+)\\s+sleeping,\\s+(\\d+)\\s+threads.*"), 
			new String[]{"proctot", "procrun", "procstuck", "proczzz", "procthreads"});
		topMapping.put(Pattern.compile("CPU usage:\\s+([0-9\\.]+)%\\s+user,\\s+([0-9\\.]+)%\\s+sys,\\s+([0-9\\.]+)%\\s+idle.*"), 
				new String[]{"cpuuser", "cpusys", "cpuidle"});
		topMapping.put(Pattern.compile("MemRegions:\\s+(\\d+)\\s+total,\\s+(\\d+)M\\s+resident,\\s+(\\d+)M\\s+private,\\s+(\\d+)M\\s+shared.*"), 
			new String[]{"memregtot", "memregres", "memregpriv", "memregshare"});
		topMapping.put(Pattern.compile("Swap:\\s+(\\d+)B\\s+\\+\\s+(\\d+)B\\s+free."), 
			new String[]{"swapused", "swapfree"});
		topMapping.put(Pattern.compile("PhysMem:\\s+(\\d+)M\\s+used\\s+.*"), 
				new String[]{"memused"});
		topMapping.put(Pattern.compile("PhysMem:\\s+(\\d+)G\\s+used\\s+.*"), 
				new String[]{"memusedgigs"});
		topMapping.put(Pattern.compile("PhysMem:\\s+\\d+[MG]{1}\\s+used\\s+\\((\\d+)M wired\\).*"), 
				new String[]{"memwired"});
		topMapping.put(Pattern.compile("PhysMem:\\s+\\d+[MG]{1}\\s+used\\s+\\((\\d+)G wired\\).*"), 
				new String[]{"memwiredgigs"});
		topMapping.put(Pattern.compile("PhysMem:\\s+\\d+[MG]{1}\\s+used\\s+\\(\\d+[MG]{1} wired\\),\\s(\\d+)M\\s+unused."), 
				new String[]{"memfree"});
		topMapping.put(Pattern.compile("PhysMem:\\s+\\d+[MG]{1}\\s+used\\s+\\(\\d+[MG]{1} wired\\),\\s(\\d+)G\\s+unused."), 
				new String[]{"memfreegigs"});
		topMapping.put(Pattern.compile("Disks:\\s+(\\d+)\\/\\d+[BKMGT]{1}\\s+read,\\s+(\\d+)\\/\\d+[BKMGT]{1}\\s+written."),
				new String[]{"diskreads","diskwrites"});
		allCommands.put("top", new UnixCommand(new String[]{"top","-l", "1", "-n", "0", "-S"}, commandTypes.REGEXDIM, defaultignores, 0, true, topMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("top", "la1"), new MetricDetail("LoadAverage", "1 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "la5"), new MetricDetail("LoadAverage", "5 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "la15"), new MetricDetail("LoadAverage", "15 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "proctot"), new MetricDetail("Processes", "Total", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "procrun"), new MetricDetail("Processes", "Running", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "proczzz"), new MetricDetail("Processes", "Sleeping", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "procstuck"), new MetricDetail("Processes", "Stuck", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "procthreads"), new MetricDetail("KernelThreads", "Runnable", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memregtot"), new MetricDetail("MemoryDetailed", "Regions/Total", "regions", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memregres"), new MetricDetail("MemoryDetailed", "Regions/Resident", "kb", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memregpriv"), new MetricDetail("MemoryDetailed", "Regions/Private", "kb", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memregshare"), new MetricDetail("MemoryDetailed", "Regions/Shared", "kb", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swapused"), new MetricDetail("MemoryDetailed", "Swap/Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swapfree"), new MetricDetail("MemoryDetailed", "Swap/Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuuser"), new MetricDetail("CPU", "User", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuidle"), new MetricDetail("CPU", "Idle", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpusys"), new MetricDetail("CPU", "System", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memused"), new MetricDetail("Memory", "Used", "kb", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memusedgigs"), new MetricDetail("Memory", "Used", "kb", metricTypes.NORMAL, 1048576));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memwired"), new MetricDetail("Memory", "Wired", "kb", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memwiredgigs"), new MetricDetail("Memory", "Wired", "kb", metricTypes.NORMAL, 1048576));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memfree"), new MetricDetail("Memory", "Free", "kb", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memfreegigs"), new MetricDetail("Memory", "Free", "kb", metricTypes.NORMAL, 1048576));
		allMetrics.put(CommandMetricUtils.mungeString("top", "diskreads"), new MetricDetail("DiskIO", "Reads Per Interval", "reads", metricTypes.NORMAL, 1048576));
		allMetrics.put(CommandMetricUtils.mungeString("top", "diskwrites"), new MetricDetail("DiskIO", "Writes Per Interval", "writes", metricTypes.NORMAL, 1048576));
		
		HashMap<Pattern, String[]> vm_statMapping = new HashMap<Pattern, String[]>();
				vm_statMapping.put(Pattern.compile("\\s*([^:]+):\\s+(\\d+)\\.*"), new String[]{kColumnMetricName, kColumnMetricValue});	
		allCommands.put("vm_stat", new UnixCommand(new String[]{"vm_stat"}, commandTypes.REGEXDIM, defaultignores, 0, vm_statMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages free"),new MetricDetail("Page", "Free", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages active"),new MetricDetail("Page", "Active", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages inactive"),new MetricDetail("Page", "Inactive", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages speculative"),new MetricDetail("Page", "Speculative", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages throttled"),new MetricDetail("Page", "Throttled", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages wired down"),new MetricDetail("Page", "Wired down", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages purgeable"),new MetricDetail("Page", "Purgeable", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "\"Translation faults\""),new MetricDetail("Faults", "VM Translation Faults", "faults", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages copy-on-write"),new MetricDetail("Page", "Copy-on-write", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages zero filled"),new MetricDetail("Page", "Zero filled", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages reactivated"),new MetricDetail("Page", "Reactivated", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages purged"),new MetricDetail("Page", "Purged", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "File-backed pages"),new MetricDetail("Page", "File-backed", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Anonymous pages"),new MetricDetail("Page", "Anonymous", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages stored in compressor"),new MetricDetail("Page", "Stored in compressor", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pages occupied by compressor"),new MetricDetail("Page", "Occupied by compressor", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Decompressions"),new MetricDetail("Page", "Decompressions", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Compressions"),new MetricDetail("Page", "Compressions", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pageins"),new MetricDetail("Page", "Paged In", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Pageouts"),new MetricDetail("Page", "Paged Out", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Swapins"),new MetricDetail("Page", "Swapped In", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vm_stat", "Swapouts"),new MetricDetail("Page", "Swapped Out", "pages", metricTypes.DELTA, 1));		
		
		HashMap<Pattern, String[]> vmstatTotalsMapping = new HashMap<Pattern, String[]>();
		vmstatTotalsMapping.put(Pattern.compile("\\s*\"*([^:\"]+)\"*:\\s+(\\d+)\\.*"), new String[]{kColumnMetricName, kColumnMetricValue});	
		allCommands.put("VmstatTotals", new UnixCommand(new String[]{"vm_stat"}, commandTypes.REGEXDIM, defaultignores, 0, vmstatTotalsMapping));
	}
}
